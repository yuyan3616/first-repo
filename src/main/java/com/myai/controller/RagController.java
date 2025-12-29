package com.myai.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public class RagController {
    private final VectorStore vectorStore;

    // 构造器注入VectorStore（用于操作Redis向量数据库）
    public RagController(VectorStore store) {
        this.vectorStore = store;
    }

    // 文本向量化入库接口
    @PostMapping("/importData")
    public String importData(@RequestParam("data") String data){
        // 构建Document对象，封装待向量化的文本
        Document document = Document.builder()
                .text(data)
                .build();
        // 将Document（自动完成向量化）存入Redis向量数据库
        vectorStore.add(List.of(document));
        return "success";
    }
    @PostMapping("/search")
    public List<Document> search(@RequestParam("query") String query) {
        // 构建搜索请求：设置多维度检索参数
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(10) // 返回相似度前10的结果
                .query(query) // 传入用户查询问题
                .similarityThreshold(0.5) // 设置相似度阈值，过滤低相似度结果

                .build();
        // 执行向量相似度搜索
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return documents;
    }
}
