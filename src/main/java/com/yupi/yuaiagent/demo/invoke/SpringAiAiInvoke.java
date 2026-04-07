package com.yupi.yuaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("SpringAiAiInvoke 中的 dashscopeChatModel hashCode: " + dashscopeChatModel.hashCode());
//        AssistantMessage output = dashscopeChatModel.call(new Prompt("你好，我是天下最帅的男人"))
//                .getResult()
//                .getOutput();
//        System.out.println("---------------------");
//        System.out.println(output.getText());
    }
}
