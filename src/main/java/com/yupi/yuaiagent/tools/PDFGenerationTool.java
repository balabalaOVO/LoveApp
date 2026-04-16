package com.yupi.yuaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.yupi.yuaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF, support markdown image syntax ![alt](url) for inserting images") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);

                // 正则匹配 Markdown 图片语法 ![alt](url)
                java.util.regex.Pattern imagePattern = java.util.regex.Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
                java.util.regex.Matcher matcher = imagePattern.matcher(content);
                
                int lastEnd = 0;
                while (matcher.find()) {
                    // 添加图片前的文本
                    if (matcher.start() > lastEnd) {
                        String text = content.substring(lastEnd, matcher.start());
                        document.add(new Paragraph(text).setFont(font));
                    }
                    
                    // 获取图片URL并下载
                    String imageUrl = matcher.group(1);
                    try {
                        byte[] imageBytes = cn.hutool.http.HttpUtil.downloadBytes(imageUrl);
                        com.itextpdf.layout.element.Image img = new com.itextpdf.layout.element.Image(
                                com.itextpdf.io.image.ImageDataFactory.create(imageBytes)
                        );
                        // 图片缩放以适应页面宽度
                        img.setAutoScale(true);
                        document.add(img);
                    } catch (Exception e) {
                        // 下载或添加图片失败时，作为普通文本添加
                        document.add(new Paragraph("\n[图片加载失败: " + imageUrl + "]\n").setFont(font));
                    }
                    
                    lastEnd = matcher.end();
                }
                
                // 添加剩余的文本
                if (lastEnd < content.length()) {
                    String remainingText = content.substring(lastEnd);
                    document.add(new Paragraph(remainingText).setFont(font));
                }
            }
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}

