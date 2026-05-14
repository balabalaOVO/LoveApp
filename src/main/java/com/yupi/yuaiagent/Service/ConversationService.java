package com.yupi.yuaiagent.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yupi.yuaiagent.entity.Conversation;
import com.yupi.yuaiagent.entity.MessageEntity;
import com.yupi.yuaiagent.mapper.ConversationMapper;
import com.yupi.yuaiagent.mapper.MessageEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final MessageEntityMapper messageEntityMapper;

    public Conversation getOrCreateConversation(Long userId, String chatKey, String appType, String firstMessage) {
        Conversation conv = conversationMapper.selectOne(
            new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getChatKey, chatKey)
                .eq(Conversation::getUserId, userId));
        if (conv != null) {
            return conv;
        }
        String title = "";
        if (firstMessage != null && !firstMessage.isBlank()) {
            title = firstMessage.length() > 50 ? firstMessage.substring(0, 50) : firstMessage;
        }
        conv = new Conversation();
        conv.setUserId(userId);
        conv.setChatKey(chatKey);
        conv.setAppType(appType);
        conv.setTitle(title);
        conv.setCreatedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conv);
        return conv;
    }

    public List<Conversation> listUserConversations(Long userId, String appType) {
        LambdaQueryWrapper<Conversation> qw = new LambdaQueryWrapper<Conversation>()
            .eq(Conversation::getUserId, userId)
            .orderByDesc(Conversation::getUpdatedAt);
        if (appType != null && !appType.isBlank()) {
            qw.eq(Conversation::getAppType, appType);
        }
        return conversationMapper.selectList(qw);
    }

    public List<MessageEntity> getMessages(Long conversationId) {
        return messageEntityMapper.selectList(
            new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getConversationId, conversationId)
                .orderByAsc(MessageEntity::getCreatedAt));
    }

    public MessageEntity saveUserMessage(Long conversationId, String content) {
        return saveMessage(conversationId, "user", content);
    }

    public MessageEntity saveAssistantMessage(Long conversationId, String content) {
        return saveMessage(conversationId, "assistant", content);
    }

    public MessageEntity saveMessage(Long conversationId, String role, String content) {
        MessageEntity msg = new MessageEntity();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        messageEntityMapper.insert(msg);
        Conversation conv = new Conversation();
        conv.setId(conversationId);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateById(conv);
        return msg;
    }

    public Conversation findByChatKey(String chatKey) {
        return conversationMapper.selectOne(
            new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getChatKey, chatKey));
    }

    public Conversation findByChatKeyAndUser(String chatKey, Long userId) {
        return conversationMapper.selectOne(
            new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getChatKey, chatKey)
                .eq(Conversation::getUserId, userId));
    }

    public boolean deleteConversation(String chatKey, Long userId) {
        Conversation conv = findByChatKeyAndUser(chatKey, userId);
        if (conv == null) return false;
        messageEntityMapper.delete(
            new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getConversationId, conv.getId()));
        conversationMapper.deleteById(conv.getId());
        return true;
    }
}
