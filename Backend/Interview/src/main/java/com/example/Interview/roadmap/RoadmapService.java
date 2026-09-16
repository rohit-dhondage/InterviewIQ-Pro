package com.example.Interview.roadmap;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.exception.ApiException;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapItemRepository roadmapItemRepository;
    private final StudentRepository studentRepository;
    private final ChatClient chatClient;

    public List<RoadmapItem> getRoadmap(User user) {
        Student student = resolveStudent(user);
        return roadmapItemRepository.findByStudentIdOrderByIdAsc(student.getId());
    }

    /**
     * AI generates a personalized 8-10 topic roadmap based on:
     *  - student's target companies
     *  - preferred study topics
     *  - interview score gaps
     */
    @Transactional
    public List<RoadmapItem> generateRoadmap(User user) {
        Student student = resolveStudent(user);

        // Clear old roadmap before generating a fresh one
        roadmapItemRepository.deleteByStudentId(student.getId());

        String targetCompanies  = student.getTargetCompanies().isEmpty()
                ? "general tech companies" : String.join(", ", student.getTargetCompanies());
        String preferredTopics  = student.getPreferredStudyTopics().isEmpty()
                ? "not specified" : String.join(", ", student.getPreferredStudyTopics());
        String interviewScore   = student.getInterviewScore() == null ? "not taken yet"
                : String.valueOf(student.getInterviewScore());

        String prompt = """
                You are a campus placement coach. Generate a concise, actionable study roadmap
                for a student targeting: %s.
                Preferred topics: %s. Interview score so far: %s/100.
                
                Return EXACTLY 8 roadmap items as a numbered list (no extra text), one per line, like:
                1. Topic Name|recommended resource URL or book name
                2. Topic Name|recommended resource URL or book name
                ...
                Focus on topics most commonly tested at those companies.
                """.formatted(targetCompanies, preferredTopics, interviewScore);

        String raw = chatClient.prompt().user(prompt).call().content();

        // Parse the numbered list
        String[] lines = raw.strip().split("\\n");
        for (String line : lines) {
            line = line.replaceAll("^\\d+\\.\\s*", "").strip();
            String[] parts = line.split("\\|", 2);
            if (parts.length == 0 || parts[0].isBlank()) continue;
            String topic    = parts[0].strip();
            String resource = parts.length > 1 ? parts[1].strip() : null;

            RoadmapItem item = RoadmapItem.builder()
                    .student(student)
                    .topic(topic)
                    .status(RoadmapItem.Status.NOT_STARTED)
                    .recommendedResource(resource)
                    .build();
            roadmapItemRepository.save(item);
        }

        return roadmapItemRepository.findByStudentIdOrderByIdAsc(student.getId());
    }

    @Transactional
    public RoadmapItem updateStatus(User user, Long itemId, RoadmapItem.Status status) {
        Student student = resolveStudent(user);
        RoadmapItem item = roadmapItemRepository.findByIdAndStudentId(itemId, student.getId())
                .orElseThrow(() -> new ApiException("Roadmap item not found", HttpStatus.NOT_FOUND));
        item.setStatus(status);
        return roadmapItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(User user, Long itemId) {
        Student student = resolveStudent(user);
        RoadmapItem item = roadmapItemRepository.findByIdAndStudentId(itemId, student.getId())
                .orElseThrow(() -> new ApiException("Roadmap item not found", HttpStatus.NOT_FOUND));
        roadmapItemRepository.delete(item);
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Student profile not found", HttpStatus.NOT_FOUND));
    }
}
