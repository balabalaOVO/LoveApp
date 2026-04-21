package com.yupi.yuaiagent.Agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayDeque;
import java.util.Queue;

@SpringBootTest
class YuManusTest {

    @Resource
    private YuManus yuManus;

    @Test
    void run() {

        String userPrompt = """  
                介绍一下你自己
                """;
        String answer = yuManus.run(userPrompt);
        System.out.println("==========================="
                + answer + "===========================");
        Assertions.assertNotNull(answer);
    }
}
