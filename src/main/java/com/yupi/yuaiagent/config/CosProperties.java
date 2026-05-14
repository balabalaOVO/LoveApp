package com.yupi.yuaiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cos")
public class CosProperties {
    private String secretId;
    private String secretKey;
    private String bucket;
    private String region;
    /** 签名 URL 有效期（分钟），默认 60 */
    private int urlExpireMinutes = 60;
    /** 文件存储路径前缀 */
    private String pathPrefix = "agent-files";
}
