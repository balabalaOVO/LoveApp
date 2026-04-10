package com.yupi.yuaiagent.tools;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
public class EmailTools {

    @Resource
    JavaMailSender mailSender;
    @Tool(description = "The subject and body of an email sent need to be provided by an AI model based on the context of the conversation")
    public String sendEmail(
            @ToolParam(description = "The recipient's email address") String to,
            @ToolParam(description = "subject of the mail") String subject,
            @ToolParam(description = "The main body of the email") String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            //如果需要，可以指定发件人，通常与配置的username一致
            message.setFrom("balabala_996@qq.com");
            mailSender.send(message);
            log.info("邮件已成功发送给{}", to);
            return "邮件已成功发送给 " + to;
        } catch (Exception e) {
            return "邮件发送失败: " + e.getMessage();
        }
    }
}