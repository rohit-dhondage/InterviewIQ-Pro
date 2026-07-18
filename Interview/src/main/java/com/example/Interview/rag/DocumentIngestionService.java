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

        // TokenTextSplitter is Spring AI's built-in chunker — same lesson as Jwts.builder(): use the library's tool, don't hand-roll splitting logic
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(rawDocs);

        vectorStore.add(chunks); // embeds each chunk and stores it — happens automatically
        System.out.println("Ingested " + chunks.size() + " chunks into the vector store.");
    }
}