package com.yupi.yuaiagent.Tools;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;

import static com.yupi.yuaiagent.demo.invoke.TestApiKey.SECRET_ID;
import static com.yupi.yuaiagent.demo.invoke.TestApiKey.SECRET_KEY;

public class TencentTranslator {
    


    private static final String REGION = "ap-beijing";

    private static final String PROJECT_ID = "0"; // 默认为0，控制台创建项目后替换为真实ID


    /**
     * 文本翻译
     * @param sourceText 待翻译的文本
     * @param sourceLang 源语言（如：zh 表示中文，en 表示英文）
     * @param targetLang 目标语言
     * @return 翻译结果
     */
    public static String translate(String sourceText, String sourceLang, String targetLang) {
        try {
            // 1. 创建认证对象
            Credential cred = new Credential(SECRET_ID, SECRET_KEY);
            
            // 2. 创建客户端
            TmtClient client = new TmtClient(cred, REGION);
            
            // 3. 创建请求对象
            TextTranslateRequest req = new TextTranslateRequest();
            req.setSourceText(sourceText);
            req.setSource(sourceLang);
            req.setTarget(targetLang);
            req.setProjectId(Long.parseLong(PROJECT_ID));

            // 4. 发送请求并获取响应
            TextTranslateResponse resp = client.TextTranslate(req);
            
            // 5. 返回翻译结果
            return resp.getTargetText();
            
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            return "翻译失败：" + e.getMessage();
        }
    }
    
    // 快速测试
    public static void main(String[] args) {
        String result = translate("你好，腾讯云，我爱学英语", "zh", "en");
        System.out.println("翻译结果：" + result);
    }
}
