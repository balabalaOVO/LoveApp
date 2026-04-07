package com.yupi.yuaiagent.rag;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

public class test extends DocumentRetrievalAdvisor {
    public test(DocumentRetriever retriever) {
        super(retriever);
    }

    public test(DocumentRetriever retriever, PromptTemplate promptTemplate) {
        super(retriever, promptTemplate);
    }

    public test(DocumentRetriever retriever, PromptTemplate promptTemplate, int order) {
        super(retriever, promptTemplate, order);
    }
}
