package com.example.Interview.interview;

import com.example.Interview.auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    // --- Scheduling ---

    @PostMapping("/schedule")
    public MockInterviewSession schedule(@AuthenticationPrincipal User user, @RequestBody ScheduleRequest request) {
        return mockInterviewService.schedule(user, request);
    }

    @GetMapping("/upcoming/me")
    public List<MockInterviewSession> upcoming(@AuthenticationPrincipal User user) {
        return mockInterviewService.getUpcomingSessions(user);
    }

    @GetMapping("/history/me")
    public List<MockInterviewSession> history(@AuthenticationPrincipal User user) {
        return mockInterviewService.getHistory(user);
    }

    // --- Running a session ---

    @PostMapping("/{sessionId}/start")
    public InterviewResponse start(@AuthenticationPrincipal User user, @PathVariable String sessionId) {
        return mockInterviewService.startSession(user, sessionId);
    }

    @PostMapping("/{sessionId}/answer")
    public InterviewResponse answer(@AuthenticationPrincipal User user, @PathVariable String sessionId, @RequestBody AnswerRequest request) {
        return mockInterviewService.processAnswer(user, sessionId, request.answer());
    }

    // --- DTOs ---
    public record ScheduleRequest(
            String company, String role, String round,
            MockInterviewSession.InterviewType interviewType, LocalDateTime scheduledAt) {}

    public record AnswerRequest(String answer) {}

    public record InterviewResponse(String sessionId, String question, boolean finished, String feedback) {}
}