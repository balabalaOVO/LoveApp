package com.yupi.yuaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class HttpAiInvoke {
    public static void main(String[] args) {
        // 请替换为您的真实 API Key（可从环境变量或配置中获取）
        String apiKey = TestApiKey.ApiKey;

        // 请求 URL
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 构建请求体 JSON
        JSONObject body = new JSONObject();
        body.set("model", "qwen-plus");

        // 构建 messages 数组
        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        systemMsg.set("content", "You are a helpful assistant.");

        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", "你是谁？");

        body.set("input", new JSONObject().set("messages", new JSONObject[] {systemMsg, userMsg}));
        body.set("parameters", new JSONObject().set("result_format", "message"));

        // 发送 POST 请求
        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .execute()) {

            // 获取响应状态码和内容
            int status = response.getStatus();
            String result = response.body();

            System.out.println("Status: " + status);
            System.out.println("Response: " + result);

            // 如果需要解析响应，可以使用 JSONUtil 处理
            if (status == 200) {
                JSONObject jsonResult = JSONUtil.parseObj(result);
                // 进一步处理...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}