package com.yupi.yuaiagent.tools;

import com.yupi.yuaiagent.Service.CosFileService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

//    @Value("${search-api.api-key}")
//    private String searchApiKey;

    @Bean
    public FileOperationTool fileOperationTool() {
        return new FileOperationTool();
    }

//    @Bean
//    public WebSearchTool webSearchTool() {
//        return new WebSearchTool(searchApiKey);
//    }

    @Bean
    public WebScrapingTool webScrapingTool() {
        return new WebScrapingTool();
    }

    @Bean
    public ResourceDownloadTool resourceDownloadTool(CosFileService cosFileService) {
        return new ResourceDownloadTool(cosFileService);
    }

    @Bean
    public TerminalOperationTool terminalOperationTool() {
        return new TerminalOperationTool();
    }

    @Bean
    public PDFGenerationTool pdfGenerationTool(CosFileService cosFileService) {
        return new PDFGenerationTool(cosFileService);
    }

    @Bean
    public DateTimeTools dateTimeTools() {
        return new DateTimeTools();
    }

    @Bean
    public EmailTools emailTools() {
        return new EmailTools();
    }

    @Bean
    public ImageSearchTool imageSearchTool() {
        return new ImageSearchTool();
    }

    @Bean
    public TerminateTool terminateTool() {
        return new TerminateTool();
    }

    @Bean
    public ToolCallback[] allTools(
            FileOperationTool fileOperationTool,
//            WebSearchTool webSearchTool,
            WebScrapingTool webScrapingTool,
            ResourceDownloadTool resourceDownloadTool,
            TerminalOperationTool terminalOperationTool,
            PDFGenerationTool pdfGenerationTool,
            DateTimeTools dateTimeTools,
            EmailTools emailTools,
            ImageSearchTool imageSearchTool,
            TerminateTool terminateTool
    ) {
        return ToolCallbacks.from(
                fileOperationTool,
//                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                dateTimeTools,
                emailTools,
                imageSearchTool,
                terminateTool
        );
    }
}
