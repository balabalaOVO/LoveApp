package com.yupi.yuaiagent.Agent;

import com.yupi.yuaiagent.Advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class YuManus extends ToolCallAgent {  
  
    public YuManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);  
        this.setName("yuManus");  
        String SYSTEM_PROMPT = """  
                You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                
                IMPORTANT: If you need to ask the user for clarification or additional information, 
                include [ASK_USER] followed by your question in your response. 
                Format: [ASK_USER]{your specific question}
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                If you need user input to proceed, use [ASK_USER]{question} format.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);  
        this.setMaxSteps(getMaxSteps());
        // 初始化客户端  
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors((Advisor)new MyLoggerAdvisor())
                .build();  
        this.setChatClient(chatClient);  
    }  
}
