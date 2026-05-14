package com.yupi.yuaiagent.chatMemory;

import com.yupi.yuaiagent.Service.ConversationService;
import com.yupi.yuaiagent.entity.Conversation;
import com.yupi.yuaiagent.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MysqlChatMemory implements ChatMemoryRepository {

    private final ConversationService conversationService;

    @Override
    public List<String> findConversationIds() {
        // Not scoped by user here — the ChatMemoryRepository interface has no user context.
        // The controller-level endpoints provide user-scoped conversation listing.
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Conversation conv = conversationService.findByChatKey(conversationId);
        if (conv == null) {
            return List.of();
        }
        List<MessageEntity> entities = conversationService.getMessages(conv.getId());
        List<Message> messages = new ArrayList<>();
        for (MessageEntity entity : entities) {
            Message msg = switch (entity.getRole()) {
                case "user" -> new UserMessage(entity.getContent());
                case "assistant" -> new AssistantMessage(entity.getContent());
                default -> null;
            };
            if (msg != null) {
                messages.add(msg);
            }
        }
        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Conversation conv = conversationService.findByChatKey(conversationId);
        if (conv == null) {
            log.warn("MysqlChatMemory: conversation not found for chatKey={}, skipping save", conversationId);
            return;
        }
        for (Message message : messages) {
            String role = switch (message.getMessageType()) {
                case USER -> "user";
                case ASSISTANT -> "assistant";
                default -> null;
            };
            if (role != null && message.getText() != null && !message.getText().isBlank()) {
                conversationService.saveMessage(conv.getId(), role, message.getText());
            }
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        // Not supported without userId context.
        // User-scoped delete is handled by ConversationService.deleteConversation.
    }
}
