package com.yupi.yuaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.yupi.yuaiagent.Service.CosFileService;
import com.yupi.yuaiagent.config.AgentContextHolder;
import com.yupi.yuaiagent.constant.FileConstant;
import com.yupi.yuaiagent.model.FileDownloadInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.nio.file.Path;

public class ResourceDownloadTool {

    private final CosFileService cosFileService;

    public ResourceDownloadTool(CosFileService cosFileService) {
        this.cosFileService = cosFileService;
    }

    @Tool(description = "Download a resource from a given URL. The file will be uploaded to cloud storage and a download link provided.")
    public String downloadResource(
            @ToolParam(description = "URL of the resource to download") String url,
            @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            HttpUtil.downloadFile(url, new File(filePath));

            if (cosFileService == null) {
                return "Resource downloaded successfully to: " + filePath;
            }
            AgentContextHolder.AgentContext ctx = AgentContextHolder.get();
            Long userId = ctx != null ? ctx.userId() : 0L;
            String chatId = ctx != null ? ctx.chatId() : "unknown";
            FileDownloadInfo info = cosFileService.uploadFile(userId, chatId, Path.of(filePath));
            return String.format(
                "文件已下载并上传到云端。 [FILE:name=%s|url=%s|cosKey=%s|size=%d]",
                fileName, info.getUrl(), info.getCosKey(), info.getSize());
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
