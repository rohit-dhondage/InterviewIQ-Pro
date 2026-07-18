package com.example.Interview.rag;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doubts")
@RequiredArgsConstructor
public class DoubtController {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    @GetMapping("/status")
    public String status() {
        return "rag-doubt-solver: scaffolded";
    }

    // Temporary — just to confirm the Groq connection works. We'll replace this with real RAG logic next.
    @GetMapping("/test")
    public String test(@RequestParam String question) {
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
    @GetMapping("/debug-key")
    public String debugKey(@Value("${spring.ai.openai.api-key}") String key) {
        if (key == null || key.isBlank()) {
            return "API key is NULL or EMPTY";
        }
        return "Key starts with: " + key.substring(0, Math.min(6, key.length())) + "... (length: " + key.length() + ")";
    }
        @GetMapping("/ask")
        public String ask(@RequestParam String question) {
            List<Document> relevant = vectorStore.similaritySearch(question);

            String context = relevant.stream()
                    .map(Document::getFormattedContent)
                    .reduce("", (a, b) -> a + "\n" + b);

            return chatClientBuilder.build()
                    .prompt()
                    .user(u -> u.text("""
                Answer the student's question using only the context below.
                If the context doesn't contain the answer, say so honestly.

                Context:
                {context}

                Question: {question}
                """)
                            .param("context", context)
                            .param("question", question))
                    .call()
                    .content();
        }

}