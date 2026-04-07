package com.yupi.yuaiagent.App;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testDoChat() {

        //chatId随机生成  message自定义
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "法国";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "冰淇淋";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "他的首都在哪？";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void TestChatWithReport() {
        //chatId随机生成  message自定义
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮, 我想让我的另一半(编程导航) 更爱我，但是我不知道怎么做";

        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);

    }

    @Test
    void doChatWithRag() {

        String message = "我已经结婚了，但婚后关系不太密切，怎么办？";
        String uuid = UUID.randomUUID().toString();

        loveApp.doChatWithRag(message, uuid);
    }

    @Test
    void doChatWithRagAndFilter() {
        String chatId = UUID.randomUUID().toString();

        log.info("========== 测试1：已婚状态过滤 ==========");
        String message1 = "我已经结婚了，但婚后关系不太密切，怎么办？";
        String response1 = loveApp.doChatWithRagAndFilter(message1, chatId, "已婚");
        Assertions.assertNotNull(response1);

        log.info("========== 测试2：单身状态过滤 ==========");
        String message2 = "如何在校园里认识女生呢？";
        String response2 = loveApp.doChatWithRagAndFilter(message2, chatId, "单身");
        Assertions.assertNotNull(response2);

        log.info("========== 测试3：恋爱状态过滤 ==========");
        String message3 = "和女朋友经常因为小事吵架，怎么办？";
        String response3 = loveApp.doChatWithRagAndFilter(message3, chatId, "恋爱");
        Assertions.assertNotNull(response3);

        log.info("========== 测试4：不过滤（所有状态） ==========");
        String message4 = "恋爱中有什么需要注意的吗？";
        String response4 = loveApp.doChatWithRagAndFilter(message4, chatId, null);
        Assertions.assertNotNull(response4);
    }


    @Resource
    VectorStore loveAppVectorStore;

    @Test
    void doChatWithRadAndFilter() {
        String chatId = UUID.randomUUID().toString();

        log.info("========== 测试2：恋爱对象推荐 ==========");
        List<Document> 对象 = loveAppVectorStore.similaritySearch("内向 拍照 猫狗");
        

        String message2 = "我性格比较内向，喜欢拍照，喜欢猫猫狗狗，推荐一个恋爱对象";
        String response2 = loveApp.doChatWithRagAndFilter(message2, chatId, "恋爱对象推荐");
        Assertions.assertNotNull(response2);

    }

    @Test
    void testSimpleRetrieve() {
        List<Document> docs = loveAppVectorStore.similaritySearch("内向 喜欢拍照 猫狗");
        System.out.println("检索到文档数：" + docs.size());
        docs.forEach(doc -> System.out.println(doc.getText()));
    }



}