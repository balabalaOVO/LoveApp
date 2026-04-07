package com.yupi.yuaiagent.Advisor;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RetrievalAugmentationAdvisor {

    @Resource
    VectorStore loveAppVectorStore;

    //过滤元数据的构造器
    public Advisor createRetrievalAugmentationAdvisor(String statusFilter) {
        return org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(loveAppVectorStore)
                        .topK(10)
                        .filterExpression(statusFilter != null
                                ? new FilterExpressionBuilder().eq("status", statusFilter).build()
                                : null)
                        .build())
                .build();
    }

    //默认构造器 - 不过滤元数据
    @Bean
    Advisor defaultRetrievalAugmentationAdvisor() {
        return createRetrievalAugmentationAdvisor(null);
    }

}
