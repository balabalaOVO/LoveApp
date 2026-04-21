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
                You are Shuai_Manus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
        According to the user's need, select the most appropriate tool or combination of tools in one go.
        For complex tasks, break them down and execute step by step, but keep each step's output minimal.
        After using each tool, summarize the result in one sentence, and **only suggest next steps if another tool is actually needed**.
        Once the goal is achieved or no further action is required, **immediately call the `terminate` tool** without any additional explanation or follow-up suggestions.
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
