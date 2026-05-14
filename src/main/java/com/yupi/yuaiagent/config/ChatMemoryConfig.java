package com.yupi.yuaiagent.config;

import com.yupi.yuaiagent.Service.ConversationService;
import com.yupi.yuaiagent.chatMemory.MysqlChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemoryRepository chatMemoryRepository(ConversationService conversationService) {
        return new MysqlChatMemory(conversationService);
    }
}
