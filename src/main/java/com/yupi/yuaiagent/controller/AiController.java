package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.Agent.YuManus;
import com.yupi.yuaiagent.App.LoveApp;
import com.yupi.yuaiagent.Service.ConversationService;
import com.yupi.yuaiagent.Service.CosFileService;
import com.yupi.yuaiagent.Service.UserService;
import com.yupi.yuaiagent.config.AgentContextHolder;
import com.yupi.yuaiagent.entity.Conversation;
import com.yupi.yuaiagent.entity.MessageEntity;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private UserService userService;

    @Resource
    private ConversationService conversationService;

    @Resource
    private CosFileService cosFileService;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId, HttpServletRequest request) {
        AgentContextHolder.AgentContext ctx = ensureConversation(request, chatId, message);
        AgentContextHolder.set(ctx);
        try {
            return loveApp.doChat(message, chatId);
        } finally {
            AgentContextHolder.clear();
        }
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId, HttpServletRequest request) {
        AgentContextHolder.AgentContext ctx = ensureConversation(request, chatId, message);
        return loveApp.doChatByStream(message, chatId)
                .doOnSubscribe(s -> AgentContextHolder.set(ctx))
                .doFinally(s -> AgentContextHolder.clear());
    }

    @GetMapping(value = "/love_app/chat/sse")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE2(String message, String chatId, HttpServletRequest request) {
        AgentContextHolder.AgentContext ctx = ensureConversation(request, chatId, message);
        return loveApp.doChatByStream(message, chatId)
                .doOnSubscribe(s -> AgentContextHolder.set(ctx))
                .doFinally(s -> AgentContextHolder.clear())
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId, HttpServletRequest request) {
        AgentContextHolder.AgentContext ctx = ensureConversation(request, chatId, message);
        SseEmitter emitter = new SseEmitter(180000L);
        AgentContextHolder.set(ctx);
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        emitter.onCompletion(AgentContextHolder::clear);
        emitter.onError(e -> AgentContextHolder.clear());
        emitter.onTimeout(AgentContextHolder::clear);
        return emitter;
    }

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message, HttpServletRequest request) {
        Long userId = getUserId(request);
        AgentContextHolder.AgentContext ctx = new AgentContextHolder.AgentContext(userId, null);
        AgentContextHolder.set(ctx);
        YuManus yuManus = new YuManus(allTools, dashscopeChatModel);
        SseEmitter emitter = yuManus.runStream(message);
        emitter.onCompletion(AgentContextHolder::clear);
        emitter.onError(e -> AgentContextHolder.clear());
        emitter.onTimeout(AgentContextHolder::clear);
        return emitter;
    }

    @GetMapping("/file/download")
    public ResponseEntity<Void> downloadFile(@RequestParam String cosKey, @RequestParam String fileName) {
        try {
            String signedUrl = cosFileService.generateSignedUrl(cosKey, fileName);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", signedUrl)
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "文件下载失败：" + e.getMessage());
        }
    }

    @GetMapping("/conversations")
    public List<Conversation> listConversations(
            @RequestParam(defaultValue = "love_app") String appType,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return conversationService.listUserConversations(userId, appType);
    }

    @GetMapping("/conversations/{chatKey}/messages")
    public List<MessageEntity> getMessages(
            @PathVariable String chatKey,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        Conversation conv = conversationService.findByChatKeyAndUser(chatKey, userId);
        if (conv == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        return conversationService.getMessages(conv.getId());
    }

    @DeleteMapping("/conversations/{chatKey}")
    public ResponseEntity<Map<String, String>> deleteConversation(
            @PathVariable String chatKey,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        boolean deleted = conversationService.deleteConversation(chatKey, userId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }

    private AgentContextHolder.AgentContext ensureConversation(HttpServletRequest request, String chatId, String firstMessage) {
        Long userId = getUserId(request);
        conversationService.getOrCreateConversation(userId, chatId, "love_app", firstMessage);
        return new AgentContextHolder.AgentContext(userId, chatId);
    }

    private Long getUserId(HttpServletRequest request) {
        String email = (String) request.getAttribute("currentUserEmail");
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        Long userId = userService.getUserIdByEmail(email);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        return userId;
    }
}
