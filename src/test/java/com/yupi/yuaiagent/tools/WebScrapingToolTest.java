package com.yupi.yuaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class WebScrapingToolTest {

    @Test
    public void testScrapeWebPage() {
        WebScrapingTool tool = new WebScrapingTool();
        String url = "https://www.bilibili.com";
        String result = tool.scrapeWebPage(url);
        log.info(result);

        try (FileWriter writer = new FileWriter("tmp/bilibili.txt")) {
            writer.write(result);
            System.out.println("文件写入成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertNotNull(result);
    }
}
