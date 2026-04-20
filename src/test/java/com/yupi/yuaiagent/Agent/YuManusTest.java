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
        Queue<String> answers = new ArrayDeque<>();
        answers.add("旅游");
        answers.add("上海");
        answers.add("下周五");
        answers.add("高铁");
        yuManus.setUserInputProvider(question -> answers.isEmpty() ? "请继续" : answers.poll());

        String userPrompt = """  
                我要去北京
                """;
        String answer = yuManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
