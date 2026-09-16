package com.example.Interview.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentIngestionService implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_store", Integer.class);
            if (count != null && count > 0) {
                System.out.println("Vector store already populated (" + count + " records). Skipping ingestion.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Could not check vector store size, proceeding with ingestion.");
        }

        TextReader reader = new TextReader(new ClassPathResource("docs/core-java-notes.txt"));
        List<Document> rawDocs = reader.get();

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(rawDocs);

        vectorStore.add(chunks); // embeds each chunk via googleGenAiTextEmbedding, stores in pgvector
        System.out.println("Ingested " + chunks.size() + " chunks into the vector store.");
    }
}