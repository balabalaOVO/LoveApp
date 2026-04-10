package com.yupi.yuaiagent.tools;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class EmailToolsTest {

    @Resource  // 关键：让 Spring 把配置好的 Bean 注入进来
    private EmailTools emailTools;
    @Test
    public void testEmail() {
        String res = emailTools.sendEmail("1115179566@qq.com", "test", "2026-04-08T20:36:00.943+08:00  INFO 16556 --- [yu-ai-agent] [           main] com.yupi.yuaiagent.App.LoveApp           : content: 看起来邮件发送功能暂时不可用。不过别担心，我可以为您生成一份精美的PDF文档，您可以随时保存、查看或转发给朋友。\n");
        log.info(res);
    }

}