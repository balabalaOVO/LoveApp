package com.yupi.yuaiagent.tools;

import cn.hutool.http.HttpUtil;
import com.yupi.yuaiagent.Service.CosFileService;
import com.yupi.yuaiagent.config.AgentContextHolder;
import com.yupi.yuaiagent.model.FileDownloadInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class ResourceDownloadTool {

    private final CosFileService cosFileService;

    public ResourceDownloadTool(CosFileService cosFileService) {
        this.cosFileService = cosFileService;
    }

    @Tool(description = "Download a resource from a given URL. The file will be uploaded to cloud storage and a download link provided.")
    public String downloadResource(
            @ToolParam(description = "URL of the resource to download") String url,
            @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        try {
            byte[] data = HttpUtil.downloadBytes(url);

            if (cosFileService == null) {
                return "Resource downloaded successfully, size: " + data.length + " bytes (COS not configured, cannot upload).";
            }
            AgentContextHolder.AgentContext ctx = AgentContextHolder.get();
            Long userId = ctx != null ? ctx.userId() : 0L;
            String chatId = ctx != null ? ctx.chatId() : "unknown";
            FileDownloadInfo info = cosFileService.uploadBytes(userId, chatId, fileName, data);
            return String.format(
                "文件已下载并上传到云端。 [FILE:name=%s|url=%s|cosKey=%s|size=%d]",
                fileName, info.getUrl(), info.getCosKey(), info.getSize());
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
