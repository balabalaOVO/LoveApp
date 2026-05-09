package com.yupi.yuaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.yupi.yuaiagent.Service.CosFileService;
import com.yupi.yuaiagent.config.AgentContextHolder;
import com.yupi.yuaiagent.constant.FileConstant;
import com.yupi.yuaiagent.model.FileDownloadInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Path;

public class PDFGenerationTool {

    private final CosFileService cosFileService;

    public PDFGenerationTool(CosFileService cosFileService) {
        this.cosFileService = cosFileService;
    }

    @Tool(description = "Generate a PDF file with given content. The file will be uploaded to cloud storage and a download link provided.")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF, support markdown image syntax ![alt](url) for inserting images") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);

                java.util.regex.Pattern imagePattern = java.util.regex.Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
                java.util.regex.Matcher matcher = imagePattern.matcher(content);

                int lastEnd = 0;
                while (matcher.find()) {
                    if (matcher.start() > lastEnd) {
                        String text = content.substring(lastEnd, matcher.start());
                        document.add(new Paragraph(text).setFont(font));
                    }
                    String imageUrl = matcher.group(1);
                    try {
                        byte[] imageBytes = cn.hutool.http.HttpUtil.downloadBytes(imageUrl);
                        com.itextpdf.layout.element.Image img = new com.itextpdf.layout.element.Image(
                                com.itextpdf.io.image.ImageDataFactory.create(imageBytes));
                        img.setAutoScale(true);
                        document.add(img);
                    } catch (Exception e) {
                        document.add(new Paragraph("\n[图片加载失败: " + imageUrl + "]\n").setFont(font));
                    }
                    lastEnd = matcher.end();
                }
                if (lastEnd < content.length()) {
                    document.add(new Paragraph(content.substring(lastEnd)).setFont(font));
                }
            }

            return uploadAndFormat(fileName, Path.of(filePath));

        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }

    private String uploadAndFormat(String fileName, Path localPath) {
        if (cosFileService == null) {
            return "PDF generated successfully to: " + localPath;
        }
        try {
            AgentContextHolder.AgentContext ctx = AgentContextHolder.get();
            Long userId = ctx != null ? ctx.userId() : 0L;
            String chatId = ctx != null ? ctx.chatId() : "unknown";
            FileDownloadInfo info = cosFileService.uploadFile(userId, chatId, localPath);
            return String.format(
                "PDF 已生成并上传到云端，可下载查看。 [FILE:name=%s|url=%s|cosKey=%s|size=%d]",
                fileName, info.getUrl(), info.getCosKey(), info.getSize());
        } catch (Exception e) {
            return "PDF 本地生成成功：" + localPath + "，但云端上传失败：" + e.getMessage();
        }
    }
}
