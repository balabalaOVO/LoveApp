package com.yupi.yuaiagent.tools;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.yupi.yuaiagent.Service.CosFileService;
import com.yupi.yuaiagent.config.AgentContextHolder;
import com.yupi.yuaiagent.model.FileDownloadInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PDFGenerationTool {

    private final CosFileService cosFileService;

    public PDFGenerationTool(CosFileService cosFileService) {
        this.cosFileService = cosFileService;
    }

    @Tool(description = "Generate a PDF file with given content. The file will be uploaded to cloud storage and a download link provided.")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF, support markdown image syntax ![alt](url) for inserting images") String content) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PdfWriter writer = new PdfWriter(baos);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                PdfFont font = createChineseFont();
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

            byte[] pdfBytes = baos.toByteArray();
            return uploadToCos(fileName, pdfBytes);

        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }

    private PdfFont createChineseFont() {
        try {
            return PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
        } catch (IOException e) {
            try {
                return PdfFontFactory.createFont();
            } catch (IOException ex) {
                throw new RuntimeException("Cannot create PDF font", ex);
            }
        }
    }

    private String uploadToCos(String fileName, byte[] pdfBytes) {
        if (cosFileService == null) {
            return "COS 未配置，无法上传文件。";
        }
        try {
            AgentContextHolder.AgentContext ctx = AgentContextHolder.get();
            Long userId = ctx != null ? ctx.userId() : 0L;
            String chatId = ctx != null ? ctx.chatId() : "unknown";
            FileDownloadInfo info = cosFileService.uploadBytes(userId, chatId, fileName, pdfBytes);
            return String.format(
                "PDF 已生成并上传到云端，可下载查看。 [FILE:name=%s|url=%s|cosKey=%s|size=%d]",
                fileName, info.getUrl(), info.getCosKey(), info.getSize());
        } catch (Exception e) {
            return "PDF 生成成功，但云端上传失败：" + e.getMessage();
        }
    }
}
