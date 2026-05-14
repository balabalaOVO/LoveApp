package com.yupi.yuaiagent.Agent2;

import com.yupi.yuaiagent.Agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public abstract class BaseAgent {

    // 提示
    private final String systemPrompt = """  
                You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;  ;
    private final String nextStepPrompt = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                """;
    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 5;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory（需要自主维护会话上下文）
    protected List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) throws Exception {
        //1 判断状态  如果状态是 IDLE 可以运行，否则抛出异常， 然后将状态改成RUNNING
        if(state != AgentState.IDLE){
            throw new Exception("状态异常");
        }
        state = AgentState.RUNNING;
        //2创建上下文消息列表 这个列表用来存放用户提问和AI的每一轮回答  每一轮回答存放在result中
        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();

        //3循环执行完成任务  记录每一步  如何执行任务? -->
        for(int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
            currentStep = i + 1;
            String result = step();
            results.add(result);
        }
        return userPrompt;
    }

    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

}
