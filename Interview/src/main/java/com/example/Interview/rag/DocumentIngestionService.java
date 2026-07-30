package com.example.Interview.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentIngestionService implements CommandLineRunner {

    private final VectorStore vectorStore;

    @Override
    public void run(String... args) {
        TextReader reader = new TextReader(new ClassPathResource("docs/core-java-notes.txt"));
        List<Document> rawDocs = reader.get();

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(rawDocs);

        vectorStore.add(chunks); // embeds each chunk via googleGenAiTextEmbedding, stores in pgvector
        System.out.println("Ingested " + chunks.size() + " chunks into the vector store.");
    }
}