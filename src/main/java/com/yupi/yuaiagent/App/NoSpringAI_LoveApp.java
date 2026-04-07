package com.yupi.yuaiagent.App;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.yupi.yuaiagent.demo.invoke.TestApiKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NoSpringAI_LoveApp {

    public static void main(String[] args) throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一名美食评论家")
                .build());
        
        Scanner scanner = new Scanner(System.in);
        Generation gen = new Generation();
        
        while (true) {
            System.out.print("你：");
            String input = scanner.nextLine();
            if ("quit".equals(input)) break;
            
            messages.add(Message.builder().role(Role.USER.getValue()).content(input).build());
            
            GenerationResult result = gen.call(GenerationParam.builder()
                    .apiKey(TestApiKey.ApiKey)
                    .model("qwen-plus")
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build());
            
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();
            messages.add(Message.builder().role(Role.ASSISTANT.getValue()).content(response).build());
            
            System.out.println("AI：" + response);
        }
    }
}
