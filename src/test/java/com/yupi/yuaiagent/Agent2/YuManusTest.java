package com.yupi.yuaiagent.Agent2;

import com.yupi.yuaiagent.Agent.YuManus;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class YuManusTest {

    @Resource
    private YuManus2 yuManus2;

    @Test
    void run() throws Exception {
        String userPrompt = """  
                我的另一半居住在美国纽约，请帮我找到附件合适的约会地点，  
                并结合一些网络图片，制定一份详细的约会计划，  
                并以 PDF 格式输出, 文字为中文
                """;
        String answer = yuManus2.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}