package com.yupi.yuaiagent.App;

import com.yupi.yuaiagent.Advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.Advisor.RetrievalAugmentationAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";
    @Resource
    private VectorStore loveAppVectorStore;

    //服务已停止
//    @Resource
//    private Advisor loveAppRagCloudAdvisor;

    @Resource
    VectorStore pgVectorVectorStore;

    @Resource
    RetrievalAugmentationAdvisor advisorFactory;  // 注入工厂类

//    public LoveApp(ChatModel dashscopeChatModel) {
//        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
//        chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultSystem(SYSTEM_PROMPT)
//                .defaultAdvisors(
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        new MyLoggerAdvisor()
//                )
//                .build();
//    }

    //自定义构造函数
    public LoveApp(ChatModel dashscopeChatModel) {
        System.out.println("LoveApp 中的 dashscopeChatModel hashCode: " + dashscopeChatModel.hashCode());
        // 初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                ).build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();

          String content = response.getResult().getOutput().getText();
          log.info("content: {}", content);
          return content;

    }

    record LoveReport(String title, List<String> suggestions) {

    }

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport response = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话输出恋爱结果，标题为{用户名}的恋爱报告， 内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", response);
        return response;
    }

    public String doChatWithRagAndFilter(String message, String chatId, String status) {
        Advisor filteredAdvisor = advisorFactory.createRetrievalAugmentationAdvisor(status);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                //.advisors(new MyLoggerAdvisor())
                // 应用RAG增强检索服务（云知识库服务）
                //.advisors(loveAppRagCloudAdvisor)
                // 启用RAG检索增强服务(PgVector向量存储)
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 自定义RetrievalAugmentationAdvisor
                .advisors(filteredAdvisor)
                //.advisors(new QuestionAnswerAdvisor(loveAppVectorStore)) //在loadMarkdown时候为每篇文章加标签
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                //.tools(allTools)
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    public String doChatWithRag(String message, String chatId) {

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                //.advisors(new MyLoggerAdvisor())
                // 应用RAG增强检索服务（云知识库服务）
                //.advisors(loveAppRagCloudAdvisor)
                // 启用RAG检索增强服务(PgVector向量存储)
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore)) //在loadMarkdown时候为每篇文章加标签
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



}