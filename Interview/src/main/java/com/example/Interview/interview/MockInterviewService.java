package com.example.Interview.interview;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.exception.ApiException;
import com.example.Interview.resume.Resume;
import com.example.Interview.resume.ResumeRepository;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final ChatClient chatClient;
    private final MockInterviewSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final ResumeRepository resumeRepository;

    private final Map<String, Integer> questionCount = new ConcurrentHashMap<>();
    private final Map<String, StringBuilder> transcripts = new ConcurrentHashMap<>();

    private static final int QUESTIONS_PER_ROUND = 4;

    public MockInterviewSession schedule(User user, MockInterviewController.ScheduleRequest request) {
        Student student = resolveStudent(user);

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

    public List<MockInterviewSession> getUpcomingSessions(User user) {
        Student student = resolveStudent(user);
        return sessionRepository.findByStudentIdAndStatusOrderByScheduledAtAsc(
                student.getId(), MockInterviewSession.InterviewStatus.SCHEDULED);
    }

    public List<MockInterviewSession> getHistory(User user) {
        Student student = resolveStudent(user);
        return sessionRepository.findByStudentIdOrderByScheduledAtDesc(student.getId());
    }

    public MockInterviewController.InterviewResponse startSession(User user, String sessionId) {
        MockInterviewSession session = resolveOwnedSession(sessionId, user);

        session.setStatus(MockInterviewSession.InterviewStatus.IN_PROGRESS);
        session.setStartedAt(LocalDateTime.now());
        sessionRepository.save(session);

        questionCount.put(sessionId, 0);
        transcripts.put(sessionId, new StringBuilder());

        // Fetch latest resume to personalize prompt
        String resumeContext = "";
        List<Resume> resumes = resumeRepository.findByStudentIdOrderByUploadedAtDesc(session.getStudent().getId());
        if (!resumes.isEmpty()) {
            Resume latestResume = resumes.get(0);
            if (latestResume.getExtractedText() != null) {
                resumeContext = "\nCandidate's Resume Context:\n" + latestResume.getExtractedText();
            }
        }

        String systemPrompt = """
                You are a senior %s interviewer conducting a %s round for a %s
                position at %s. Ask one focused question at a time — do not ask multiple
                questions in one turn. Keep questions realistic for a campus placement
                interview at this company. After the candidate answers, briefly note
                (1-2 sentences) whether the answer covered definition, mechanism, and
                personal experience — the standard framework this candidate uses — then
                ask the next question. Stay strictly in interviewer character.
                %s
                """.formatted(session.getInterviewType(), session.getRound(), session.getRole(), session.getCompany(), resumeContext);

        String firstQuestion = chatClient.prompt()
                .system(systemPrompt)
                .user("Begin the interview with your first question.")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        transcripts.get(sessionId).append("Q1: ").append(firstQuestion).append("\n");

        return new MockInterviewController.InterviewResponse(sessionId, firstQuestion, false, null);
    }

    public MockInterviewController.InterviewResponse processAnswer(User user, String sessionId, String answer) {
        resolveOwnedSession(sessionId, user);

        int count = questionCount.getOrDefault(sessionId, 0) + 1;
        questionCount.put(sessionId, count);

        StringBuilder transcript = transcripts.computeIfAbsent(sessionId, k -> new StringBuilder());
        transcript.append("A").append(count).append(": ").append(answer).append("\n");

        boolean isFinal = count >= QUESTIONS_PER_ROUND;

        String userTurn = isFinal
                ? answer + "\n\n[This was the final question. Give overall structured feedback now: strengths, gaps, and one concrete improvement — 4-5 sentences total. Do not ask another question.]"
                : answer;

        String reply = chatClient.prompt()
                .user(userTurn)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        if (isFinal) {
            MockInterviewSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ApiException("No session found for id: " + sessionId, HttpStatus.NOT_FOUND));

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

            return new MockInterviewController.InterviewResponse(sessionId, null, true, reply);
        }

        transcript.append("Q").append(count + 1).append(": ").append(reply).append("\n");
        return new MockInterviewController.InterviewResponse(sessionId, reply, false, null);
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("No student profile found for this account", HttpStatus.NOT_FOUND));
    }

    private MockInterviewSession resolveOwnedSession(String sessionId, User user) {
        MockInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("No session found for id: " + sessionId, HttpStatus.NOT_FOUND));

        if (!session.getStudent().getUser().getId().equals(user.getId())) {
            throw new ApiException("You do not have access to this interview session", HttpStatus.FORBIDDEN);
        }

        return session;
    }
}
