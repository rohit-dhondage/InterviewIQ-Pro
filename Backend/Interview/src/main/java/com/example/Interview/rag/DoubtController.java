package com.example.Interview.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doubts")
@RequiredArgsConstructor
public class DoubtController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @GetMapping("/status")
    public String status() {
        return "rag-doubt-solver: active";
    }

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest request) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.question())
                .topK(3)
                .similarityThreshold(0.7)
                .build();

        List<Document> relevant = vectorStore.similaritySearch(searchRequest);

        String context = relevant.stream()
                .map(Document::getFormattedContent)
                .reduce("", (a, b) -> a + "\n" + b);

        String answer = chatClient
                .prompt()
                .user(u -> u.text("""
                        Answer the student's question using only the context below.
                        If the context doesn't contain the answer, say so honestly.

                        Context:
                        {context}

                        Question: {question}
                        """)
                        .param("context", context)
                        .param("question", request.question()))
                .call()
                .content();

        return new AskResponse(answer, relevant.size());
    }

    // --- DTOs ---
    public record AskRequest(String question) {}

    public record AskResponse(String answer, int sourcesUsed) {}
}