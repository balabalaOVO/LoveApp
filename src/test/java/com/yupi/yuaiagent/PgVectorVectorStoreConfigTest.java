package com.yupi.yuaiagent;

import com.yupi.yuaiagent.rag.LoveAppDocumentLoader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

//@SpringBootTest
@SpringBootTest(classes = YuAiAgentApplication.class)
@Slf4j
public class PgVectorVectorStoreConfigTest {

    @Resource
    VectorStore pgVectorVectorStore;

    @Resource
    LoveAppDocumentLoader loveAppDocumentLoader;



    @Test
    void test() {

//        List<Document> documents = List.of(
//                new Document("韩佳帅最帅", Map.of("meta1", "meta1")),
//                new Document("鱼皮最帅"),
//                new Document("万志云最帅", Map.of("meta2", "meta2")));
//        // 添加文档
//        pgVectorVectorStore.add(documents);


        //加载markdown文档到数据库中
        //加载所有文档
//        List<Document> allDocuments = loveAppDocumentLoader.loadMarkdowns();
//        log.info("总共加载到 {} 个文档", allDocuments.size());
//
//        // 分批添加，每批最多 10 个
//        int batchSize = 10;
//        for (int i = 0; i < allDocuments.size(); i += batchSize) {
//            int end = Math.min(i + batchSize, allDocuments.size());
//            List<Document> batch = allDocuments.subList(i, end);
//            log.info("添加批次：{} - {}", i, end);
//            pgVectorVectorStore.add(batch);
//        }

        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("我已经结婚了，但婚后关系不太密切，怎么办？").topK(5).build());
        Assertions.assertNotNull(results);

        // 简单输出
        System.out.println("查询结果数量：" + results.size());
        results.forEach(doc -> {
            System.out.println("内容：" + doc.getText());
            System.out.println("分数：" + doc.getScore());
            System.out.println("---");
        });
    }
}
