//package com.example.Interview.config;
//
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//@Configuration
//public class VectorStoreConfig {
//
//    @Bean
//    public VectorStore vectorStore(
//            JdbcTemplate jdbcTemplate,
//            @Qualifier("googleGenAiEmbeddingModel") EmbeddingModel embeddingModel) {
//
//        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
//                .build();
//    }
//}