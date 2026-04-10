package com.yupi.yuaiagent.Advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 100;
    }


    private ChatClientRequest before(ChatClientRequest request) {
        //log.info("AI Request: {}", request.prompt());
        log.info("AI Request Context: {}", request.context().values());

        // 打印检索到的文档内容
        Object retrievedDocuments = request.context().get("retrievedDocuments");
        if (retrievedDocuments != null) {
            log.info("=== 检索到的文档内容 ===");
            log.info("{}", retrievedDocuments);
        }

        return request;
    }


    private void observeAfter(ChatClientResponse chatClientResponse) {
        //log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
        log.info("AI Response: ");
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        chatClientRequest = before(chatClientRequest);
        ChatClientResponse chatClientResponse = chain.nextCall(chatClientRequest);
        observeAfter(chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        chatClientRequest = before(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponseFlux = chain.nextStream(chatClientRequest);
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponseFlux, this::observeAfter);
    }
}
