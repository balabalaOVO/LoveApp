package com.yupi.yuaiagent.tools;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
class DateTimeToolsTest {

    @Test
    public void testTime() {
        DateTimeTools dateTimeTools = new DateTimeTools();
        String currentDateTime = dateTimeTools.getCurrentDateTime();
        log.info(currentDateTime);
    }
}