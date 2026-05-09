package com.yupi.yuaiagent;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YuAiAgentApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    public void testCosCredentials() {
        String secretId = "";
        String secretKey = "";

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region("ap-beijing"));

        COSClient cosClient = null;
        try {
            cosClient = new COSClient(cred, config);
            boolean exists = cosClient.doesBucketExist("ai-agent-1417612611");
            System.out.println("Bucket 存在: " + exists);

            if (exists) {
                System.out.println("✓ 密钥验证成功");
            }
        } catch (Exception e) {
            System.err.println("✗ 密钥验证失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }


}
