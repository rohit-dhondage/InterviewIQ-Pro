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
    private final com.example.Interview.progress.ProgressService progressService;

    private static final int QUESTIONS_PER_ROUND = 4;

    public MockInterviewSession schedule(User user, MockInterviewController.ScheduleRequest request) {
        Student student = resolveStudent(user);

        String topicsStr = (request.topics() != null && !request.topics().isEmpty())
                ? String.join(", ", request.topics())
                : "Data Structures & Algorithms, Java, Core CS";

        String diff = request.difficulty() != null ? request.difficulty() : "Medium";
        String comp = request.company() != null && !request.company().trim().isEmpty() ? request.company() : "Target MNC";
        String role = request.role() != null ? request.role() : "Software Engineer";
        String round = request.round() != null ? request.round() : "Technical Assessment";

        MockInterviewSession session = MockInterviewSession.builder()
                .student(student)
                .company(comp)
                .role(role)
                .round(round)
                .interviewType(request.interviewType() != null ? request.interviewType() : MockInterviewSession.InterviewType.TECHNICAL)
                .topics(topicsStr)
                .difficulty(diff)
                .scheduledAt(request.scheduledAt() != null ? request.scheduledAt() : LocalDateTime.now())
                .status(MockInterviewSession.InterviewStatus.SCHEDULED)
                .build();

        return sessionRepository.save(session);
    }

    public MockInterviewController.InterviewResponse startNewSession(User user, MockInterviewController.ScheduleRequest request) {
        MockInterviewSession session = schedule(user, request != null ? request : new MockInterviewController.ScheduleRequest(
                "Target MNC", "Software Engineer", "Technical Assessment",
                MockInterviewSession.InterviewType.TECHNICAL, LocalDateTime.now(),
                List.of("Java", "Data Structures & Algorithms"), "Medium", 30
        ));
        return startSession(user, session.getId());
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
        session.setCurrentQuestionCount(0);
        session.setTranscript("");

        // Fetch latest resume to personalize prompt
        String resumeContext = "";
        List<Resume> resumes = resumeRepository.findByStudentIdOrderByUploadedAtDesc(session.getStudent().getId());
        if (!resumes.isEmpty()) {
            Resume latestResume = resumes.get(0);
            if (latestResume.getExtractedText() != null) {
                resumeContext = "\nCandidate's Resume Context:\n" + latestResume.getExtractedText();
            }
        }

        String selectedTopics = session.getTopics() != null ? session.getTopics() : "Data Structures & Algorithms, Java";
        String diffLevel = session.getDifficulty() != null ? session.getDifficulty() : "Medium";

        String systemPrompt = """
                You are a Senior Principal Interviewer at %s conducting a %s (%s round) for a %s role.
                Selected Topics: %s.
                Difficulty Level: %s.

                STRICT INSTRUCTIONS:
                1. Every question you ask MUST be strictly based on the candidate's selected topics: [%s] at [%s] difficulty level.
                2. Do NOT ask generic out-of-domain questions.
                3. Ask ONE focused, clear, and realistic technical question at a time. Include clear input/output requirements or code problem context if applicable.
                4. When the candidate answers or submits code, evaluate their exact response for technical accuracy, time/space complexity ($O(N)$), and edge cases before asking the next question.
                5. Stay strictly in character as a professional tech interviewer.
                %s
                """.formatted(
                        session.getCompany(), session.getInterviewType(), session.getRound(), session.getRole(),
                        selectedTopics, diffLevel, selectedTopics, diffLevel, resumeContext
                );

        String firstQuestion = chatClient.prompt()
                .system(systemPrompt)
                .user("Begin the technical interview by asking the first question strictly based on: " + selectedTopics)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        session.setTranscript("Q1: " + firstQuestion + "\n");
        sessionRepository.save(session);

        List<String> topicsList = java.util.Arrays.stream(selectedTopics.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return new MockInterviewController.InterviewResponse(
                sessionId, firstQuestion, false, null,
                session.getCompany(), session.getRole(), session.getRound(),
                1, QUESTIONS_PER_ROUND, 0.0, topicsList, diffLevel
        );
    }

    public MockInterviewController.InterviewResponse processAnswer(User user, String sessionId, String answer) {
        MockInterviewSession session = resolveOwnedSession(sessionId, user);

        int count = session.getCurrentQuestionCount() + 1;
        
        StringBuilder transcript = new StringBuilder(
                session.getTranscript() != null ? session.getTranscript() : ""
        );
        transcript.append("A").append(count).append(": ").append(answer).append("\n");

        boolean isFinal = count >= QUESTIONS_PER_ROUND;

        String userTurn = isFinal
                ? answer + "\n\n[This was the final question. Give overall structured feedback now: strengths, gaps, and one concrete improvement — 4-5 sentences total. ALSO, end your entire response with a single line formatted EXACTLY like this: SCORE: X, where X is an integer out of 100 representing the candidate's performance. Do not ask another question.]"
                : answer;

        String reply = chatClient.prompt()
                .user(userTurn)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        // Only update DB if LLM call succeeds
        session.setCurrentQuestionCount(count);

        if (isFinal) {
            LocalDateTime now = LocalDateTime.now();
            session.setStatus(MockInterviewSession.InterviewStatus.COMPLETED);
            session.setCompletedAt(now);
            session.setFeedback(reply);
            session.setTranscript(transcript.toString());
            if (session.getStartedAt() != null) {
            session.setDurationSeconds((int) Duration.between(session.getStartedAt(), now).getSeconds());
            }
            sessionRepository.save(session);
            
            // Extract numeric score
            Double score = 0.0;
            try {
                String[] lines = reply.split("\\r?\\n");
                for (String line : lines) {
                    if (line.trim().startsWith("SCORE:")) {
                        String num = line.substring(6).trim().replaceAll("[^0-9]", "");
                        if (!num.isEmpty()) {
                            score = Double.parseDouble(num);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore parse errors, score remains 0.0 or could be handled otherwise
            }

            // Save session overall score
            session.setOverallScore(score);
            sessionRepository.save(session);
            
            // Update student snapshot
            Student student = session.getStudent();
            student.setInterviewScore(score);
            student.updateReadinessScore();
            studentRepository.save(student);

            // Record progress
            progressService.record(student, student.getResumeScore(), student.getReadinessScore(), score, score);
            
            List<String> topicsList = session.getTopics() != null
                    ? java.util.Arrays.stream(session.getTopics().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
                    : List.of("Data Structures & Algorithms");

            return new MockInterviewController.InterviewResponse(
                    sessionId, null, true, reply,
                    session.getCompany(), session.getRole(), session.getRound(),
                    count, QUESTIONS_PER_ROUND, score,
                    topicsList, session.getDifficulty()
            );
        }

        transcript.append("Q").append(count + 1).append(": ").append(reply).append("\n");
        session.setTranscript(transcript.toString());
        sessionRepository.save(session);

        List<String> topicsList = session.getTopics() != null
                ? java.util.Arrays.stream(session.getTopics().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
                : List.of("Data Structures & Algorithms");

        return new MockInterviewController.InterviewResponse(
                sessionId, reply, false, null,
                session.getCompany(), session.getRole(), session.getRound(),
                count, QUESTIONS_PER_ROUND, 0.0,
                topicsList, session.getDifficulty()
        );
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
