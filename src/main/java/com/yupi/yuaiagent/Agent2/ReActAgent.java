package com.yupi.yuaiagent.Agent2;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yupi.yuaiagent.Agent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ReActAgent extends BaseAgent{

    // 保存了工具调用信息的响应
    private ChatResponse toolCallChatResponse;

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    public ReActAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    public Boolean think() {
        //思考步骤 判断是否需要工具
        log.info("思考中");

        messageList.add(new UserMessage(getNextStepPrompt()));//复用 BaseAgent中的messageList

        Prompt prompt = new Prompt(messageList, chatOptions);
        // 获取带工具选项的响应
        ChatResponse chatResponse = getChatClient()
                .prompt(prompt)
                .system(getSystemPrompt())
                .toolCallbacks(availableTools)
                .call()
                .chatResponse();
        // 记录响应，用于 Act
        this.toolCallChatResponse = chatResponse;
        AssistantMessage assistantMessage = chatResponse.getResult().getOutput(); //AssistantMessage 表示模型调用的输出。
        List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

        if (toolCallList.isEmpty()) {
            // 只有不调用工具时，才记录助手消息
            log.info("不调用工具时，记录助手消息");
            getMessageList().add(assistantMessage);
            return false;
        } else {
            // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
            log.info(" 需要调用工具时，无需记录助手消息");
            return true;
        }
    }

    public String act() {

        //行动阶段 -调用工具返回结果

        // 调用工具
        //1 把 prompt 和 toolCallChatResponse 交给 toolCallingManager管理并调用相应的工具
        //2 把返回结果封装到MessageList中
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());

        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        log.info(String.valueOf(toolResponseMessage));
        return String.valueOf(toolResponseMessage);

    }

    @Override
    public String step() {
        //判断思考结果  false-不需要行动，直接返回  true-需要行动
        Boolean needAct = think();
        if(needAct == false) {
            log.info("思考结束，无需行动");
            return "思考结束，无需行动";
        }
        act();
        return "行动结束";
    }


}
