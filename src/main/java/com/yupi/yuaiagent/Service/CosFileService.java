package com.yupi.yuaiagent.Service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.ResponseHeaderOverrides;
import com.qcloud.cos.region.Region;
import com.yupi.yuaiagent.config.CosProperties;
import com.yupi.yuaiagent.model.FileDownloadInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CosFileService {

    private final CosProperties cosProperties;
    private COSClient cosClient;

    @PostConstruct
    public void init() {
        String secretId = cosProperties.getSecretId();
        String maskedId = secretId != null && secretId.length() > 8
                ? secretId.substring(0, 4) + "****" + secretId.substring(secretId.length() - 4)
                : "***";
        log.info("COS client initializing: secretId={}, bucket={}, region={}",
                maskedId, cosProperties.getBucket(), cosProperties.getRegion());

        if (secretId == null || secretId.isBlank() || cosProperties.getSecretKey() == null || cosProperties.getSecretKey().isBlank()) {
            log.warn("COS credentials not configured — file upload will be disabled");
            return;
        }

        COSCredentials cred = new BasicCOSCredentials(secretId, cosProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(cred, clientConfig);
        log.info("COS client initialized successfully, bucket={}, region={}", cosProperties.getBucket(), cosProperties.getRegion());
    }

    @PreDestroy
    public void shutdown() {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }

    /**
     * 上传本地文件到 COS，返回带签名有效期（默认1小时）的下载 URL
     */
    public FileDownloadInfo uploadFile(Long userId, String conversationId, Path localPath) {
        File file = localPath.toFile();
        String fileName = file.getName();
        String cosKey = buildCosKey(userId, conversationId, fileName);

        try (FileInputStream input = new FileInputStream(file)) {
            return uploadStream(cosKey, input, file.length(), getContentType(fileName));
        } catch (Exception e) {
            log.error("COS upload failed for {}", fileName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传字节数组到 COS
     */
    public FileDownloadInfo uploadBytes(Long userId, String conversationId, String fileName, byte[] data) {
        String cosKey = buildCosKey(userId, conversationId, fileName);

        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            return uploadStream(cosKey, input, data.length, getContentType(fileName));
        } catch (Exception e) {
            log.error("COS upload failed for {}", fileName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式上传
     */
    private FileDownloadInfo uploadStream(String cosKey, InputStream input, long contentLength, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);
        String fileName = cosKey.substring(cosKey.lastIndexOf('/') + 1);
        metadata.setContentDisposition(buildContentDisposition(fileName));

        PutObjectRequest putRequest = new PutObjectRequest(cosProperties.getBucket(), cosKey, input, metadata);
        cosClient.putObject(putRequest);

        String url = generateSignedUrl(cosKey, fileName);

        log.info("COS upload success: {} ({}, {} bytes)", cosKey, contentType, contentLength);
        return new FileDownloadInfo(fileName, url, cosKey, contentLength, contentType);
    }

    /**
     * 为已存在的 COS 对象生成带签名的下载 URL
     */
    public String generateSignedUrl(String cosKey) {
        Date expiration = new Date(System.currentTimeMillis() + cosProperties.getUrlExpireMinutes() * 60 * 1000L);
        URL url = cosClient.generatePresignedUrl(cosProperties.getBucket(), cosKey, expiration, HttpMethodName.GET);
        return url.toString();
    }

    /**
     * 为已存在的 COS 对象生成带签名的下载 URL，强制浏览器下载
     */
    public String generateSignedUrl(String cosKey, String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + cosProperties.getUrlExpireMinutes() * 60 * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                cosProperties.getBucket(), cosKey, HttpMethodName.GET);
        request.setExpiration(expiration);
        ResponseHeaderOverrides headerOverrides = new ResponseHeaderOverrides();
        headerOverrides.setContentDisposition(buildContentDisposition(fileName));
        request.setResponseHeaders(headerOverrides);
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * 删除 COS 文件
     */
    public void deleteFile(String cosKey) {
        cosClient.deleteObject(cosProperties.getBucket(), cosKey);
    }

    private String buildCosKey(Long userId, String conversationId, String fileName) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s/%d/%s/%s_%s",
                cosProperties.getPathPrefix(), userId, conversationId != null ? conversationId : "unknown", uuid, fileName);
    }

    private String buildContentDisposition(String fileName) {
        boolean allAscii = fileName.chars().allMatch(c -> c < 128);
        if (allAscii) {
            return "attachment; filename=\"" + fileName + "\"";
        }
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded;
    }

    private String getContentType(String fileName) {
        try {
            String contentType = Files.probeContentType(Path.of(fileName));
            return contentType != null ? contentType : "application/octet-stream";
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}
