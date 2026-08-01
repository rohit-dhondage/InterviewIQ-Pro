package com.example.Interview.interview;

import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class MockInterviewController {

    private final ChatClient chatClient;
    private final MockInterviewSessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    private final Map<String, Integer> questionCount = new ConcurrentHashMap<>();
    private final Map<String, StringBuilder> transcripts = new ConcurrentHashMap<>();

    private static final int QUESTIONS_PER_ROUND = 4;

    // --- Scheduling ---

    @PostMapping("/schedule")
    public MockInterviewSession schedule(@RequestBody ScheduleRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new IllegalArgumentException("No student found for id: " + request.studentId()));

        MockInterviewSession session = MockInterviewSession.builder()
                .student(student)
                .company(request.company())
                .role(request.role())
                .round(request.round())
                .interviewType(request.interviewType())
                .scheduledAt(request.scheduledAt())
                .status(MockInterviewSession.InterviewStatus.SCHEDULED)
                .build();

        return sessionRepository.save(session);
    }

    @GetMapping("/upcoming/{studentId}")
    public List<MockInterviewSession> upcoming(@PathVariable Long studentId) {
        return sessionRepository.findByStudentIdAndStatusOrderByScheduledAtAsc(
                studentId, MockInterviewSession.InterviewStatus.SCHEDULED);
    }

    @GetMapping("/history/{studentId}")
    public List<MockInterviewSession> history(@PathVariable Long studentId) {
        return sessionRepository.findByStudentIdOrderByScheduledAtDesc(studentId);
    }

    // --- Running a session ---

    @PostMapping("/{sessionId}/start")
    public InterviewResponse start(@PathVariable String sessionId) {
        MockInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No session found for id: " + sessionId));

        session.setStatus(MockInterviewSession.InterviewStatus.IN_PROGRESS);
        session.setStartedAt(LocalDateTime.now());
        sessionRepository.save(session);

        questionCount.put(sessionId, 0);
        transcripts.put(sessionId, new StringBuilder());

        String systemPrompt = """
                You are a senior %s interviewer conducting a %s round for a %s
                position at %s. Ask one focused question at a time — do not ask multiple
                questions in one turn. Keep questions realistic for a campus placement
                interview at this company. After the candidate answers, briefly note
                (1-2 sentences) whether the answer covered definition, mechanism, and
                personal experience — the standard framework this candidate uses — then
                ask the next question. Stay strictly in interviewer character.
                """.formatted(session.getInterviewType(), session.getRound(), session.getRole(), session.getCompany());

        String firstQuestion = chatClient.prompt()
                .system(systemPrompt)
                .user("Begin the interview with your first question.")
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CONVERSATION_ID, sessionId))
                .call()
                .content();

        transcripts.get(sessionId).append("Q1: ").append(firstQuestion).append("\n");

        return new InterviewResponse(sessionId, firstQuestion, false, null);
    }

    @PostMapping("/{sessionId}/answer")
    public InterviewResponse answer(@PathVariable String sessionId, @RequestBody AnswerRequest request) {
        int count = questionCount.getOrDefault(sessionId, 0) + 1;
        questionCount.put(sessionId, count);

        StringBuilder transcript = transcripts.computeIfAbsent(sessionId, k -> new StringBuilder());
        transcript.append("A").append(count).append(": ").append(request.answer()).append("\n");

        boolean isFinal = count >= QUESTIONS_PER_ROUND;

        String userTurn = isFinal
                ? request.answer() + "\n\n[This was the final question. Give overall structured feedback now: strengths, gaps, and one concrete improvement — 4-5 sentences total. Do not ask another question.]"
                : request.answer();

        String reply = chatClient.prompt()
                .user(userTurn)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CONVERSATION_ID, sessionId))
                .call()
                .content();

        if (isFinal) {
            MockInterviewSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("No session found for id: " + sessionId));

            LocalDateTime now = LocalDateTime.now();
            session.setStatus(MockInterviewSession.InterviewStatus.COMPLETED);
            session.setCompletedAt(now);
            session.setFeedback(reply);
            session.setTranscript(transcript.toString());
            if (session.getStartedAt() != null) {
                session.setDurationSeconds((int) Duration.between(session.getStartedAt(), now).getSeconds());
            }
            sessionRepository.save(session);

            questionCount.remove(sessionId);
            transcripts.remove(sessionId);

            return new InterviewResponse(sessionId, null, true, reply);
        }

        transcript.append("Q").append(count + 1).append(": ").append(reply).append("\n");
        return new InterviewResponse(sessionId, reply, false, null);
    }

    // --- DTOs ---
    public record ScheduleRequest(
            Long studentId, String company, String role, String round,
            MockInterviewSession.InterviewType interviewType, LocalDateTime scheduledAt) {}

    public record AnswerRequest(String answer) {}

    public record InterviewResponse(String sessionId, String question, boolean finished, String feedback) {}
}
