package com.myai.controller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/coffee")
public class CoffeeController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    // 构造器注入+RAG增强器配置
    public CoffeeController(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;

        // 1. 构建VectorStoreDocumentRetriever（文档检索器，绑定向量知识库）
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore) // 关联向量知识库
                .topK(3) // 检索前3条相似结果
                .similarityThreshold(0.5) // 相似度阈值≥0.5
                .build();

        // 2. 构建RetrievalAugmentationAdvisor（RAG增强器，绑定检索器）
        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever) // 传入检索器
                .build();

        // 3. 配置ChatClient，绑定RAG增强器
        this.chatClient = chatClientBuilder
                .defaultAdvisors(ragAdvisor) // 启用RAG增强
                .build();
    }

    // 1. 导入QA数据到向量知识库
    @RequestMapping("/import")
    public String importData() {
        try {
            ClassPathResource resource = new ClassPathResource("QA.csv");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream());

            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            List<Document> documents = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                String question = record.get("问题");
                String answer = record.get("回答");
                String content = "问题：" + question + "\n回答：" + answer;
                documents.add(new Document(content));
            }

            csvParser.close();
            vectorStore.add(documents);
            return "成功导入 " + documents.size() + " 条QA到向量知识库";

        } catch (Exception e) {
            e.printStackTrace();
            return "导入失败：" + e.getMessage();
        }
    }

    // 2. RAG对话接口（基于知识库回答）
    @GetMapping("/chat")
    public String chat(@RequestParam("query") String query) {
        // 调用ChatClient，自动触发RAG（检索知识库+生成回答）
        return chatClient.prompt()
                .user(query)
                .call()
                .content();
    }
}
