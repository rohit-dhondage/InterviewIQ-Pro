package com.example.Interview.config;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {
    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        // In-memory vector store — fine at our scale (hundreds of chunks), no MySQL vector support needed
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
