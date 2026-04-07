package com.yupi.yuaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class LoveAppDocumentLoader {

    //ResourcePatternResolver : 加载多个文件（支持通配符 /*.md）
    public final ResourcePatternResolver resourcePatternResolver;
    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    //需要将 /document 目录下的多个markdown文档喂给AI
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>(); //markdown文档集合
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for(Resource resource : resources) {
                String filename = resource.getFilename();
//                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
//                        .withHorizontalRuleCreateDocument(true)
//                        .withIncludeCodeBlock(false)
//                        .withIncludeBlockquote(false)
//                        .withAdditionalMetadata("filename", filename) //additionalMetadata：允许您向所有创建的 Document 对象添加自定义元数据。（打标签）
//                        .build();
//                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
//                allDocuments.addAll(reader.get());

                //提取元信息
                //提取文档倒数第 3 和第 2 个字作为标签
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                String girlStatus = filename;
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename) //元信息标注
                        .withAdditionalMetadata("status", status)    //元信息标注
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                List<Document> docs = reader.get();
                allDocuments.addAll(docs);
                log.info("文档的元信息: {}", allDocuments);
                log.info("加载文档：{} | 元信息：{filename={}, status={}}",
                        filename, filename, status, docs.size());

            }
        } catch (Exception e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }

    public List<Document> loadGirlMarkdowns() {
        List<Document> allDocuments = new ArrayList<>(); //markdown文档集合
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/girl/*.md");
            for(Resource resource : resources) {
                String filename = resource.getFilename();

                //提取元信息
                //提取文档倒数第 3 和第 2 个字作为标签

                //String girlStatus = filename.substring(filename.length() - 7, filename.length() - 3);
                String girlStatus = "恋爱对象推荐";
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename) //元信息标注
                        .withAdditionalMetadata("status", girlStatus)    //元信息标注
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                List<Document> docs = reader.get();
                allDocuments.addAll(docs);
                log.info("文档的元信息: {}", allDocuments);
                log.info("加载文档：{} | 元信息：{filename={}, status={}}",
                        filename, filename, girlStatus, docs.size());

            }
        } catch (Exception e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }

}