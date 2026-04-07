package com.yupi.yuaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

public class LangChainAIInvoke {

    public static void main(String[] args) {

        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.ApiKey)
                .modelName("qwen-max")
                .build();
        String chat = qwenChatModel.chat("我叫大帅逼，快夸我哦");
        System.out.println(chat);
    }
}
