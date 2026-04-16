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

@SpringBootTest(properties = "spring.main.lazy-initialization=true")
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
    void doChatWithRagAndFilter2() {
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

    @Test
    void doChatWithTools() {
//        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");
//
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");
//
//        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");
//
//        // 测试 PDF 生成
//        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");

        testMessage("给1115179566@qq.com发送一份如何在工作中认识女生的邮件");
    }
    @Test
    void sendEmail() {
        testMessage("给1115179566@qq.com发一封测试邮件，主题叫“来自 Spring AI 的第一声问候”，内容写“嘿，这封邮件是由 AI 自动生成的，是不是很酷？");
        testMessage("给1115179566@qq.com发个日程提醒，主题是“明天下午3点项目周会”，正文提醒他准备好上周的进度报告");
        testMessage("给1115179566@qq.com发送一封感谢信，主题：“感谢参与 Spring AI 内测”，正文写：“感谢您的宝贵反馈，这是我们为您准备的一份小礼物兑换码：SPRING2026");
        testMessage("给1115179566@qq.com写封信主题：“服务器日志清理通知”，内容是：“/var/log 目录已使用 85%，请在今日内完成清理，否则系统可能变慢。");
        testMessage("给1115179566@qq.com写封信，主题是：“⚠\uFE0F 异常登录提醒”，正文内容是：“系统检测到您的账号刚刚在 美国洛杉矶 登录。如非本人操作，请立即修改密码。");
        testMessage("给1115179566@qq.com写封信，发封邮件，主题：“设计稿确认”，正文：“第三版 UI 界面已上传至 Figma，链接：https://figma.fake/spring-ai-v3，请于本周五前确认。");
        testMessage("发送一封纯英文测试邮件给 1115179566@qq.com，Subject: \"Q2 Regression Test Passed\", Body: \"All 142 test cases passed. Build version: v2.3.1. Ready for production deployment.");
        testMessage("我想测试一下换行符，请发邮件给 1115179566@qq.com，主题：“今晚月色真美”，正文第一行写：“风温柔地吹着，”，第二行写：“像你说话的声音。”");
        testMessage("发一封关于 API Key 过期的提醒给 1115179566@qq.com，主题：“API Key 即将过期提醒”，正文写：“您的 Key sk-spring-ai-demo-key 将在 7 天后过期。请尽快更新你的配置文件。");

    }
    @Test
    void testMap() {
        testMessage("寻找一些关于爱情的图片");
    }

    @Test
    void doChatWithMcp() {
        testMessage("寻找一些关于爱情的图片");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        //String answer = loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }

}