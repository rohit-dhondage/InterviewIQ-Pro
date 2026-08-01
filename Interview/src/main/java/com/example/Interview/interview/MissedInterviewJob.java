package com.example.Interview.interview;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MissedInterviewJob {

    private final MockInterviewSessionRepository repository;

    private static final long GRACE_MINUTES = 30;

    // Remember: add @EnableScheduling on your main application class for this to fire.
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void markMissedSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(GRACE_MINUTES);

        List<MockInterviewSession> overdue = repository.findByStatusAndScheduledAtBefore(
                MockInterviewSession.InterviewStatus.SCHEDULED, cutoff);

        for (MockInterviewSession session : overdue) {
            session.setStatus(MockInterviewSession.InterviewStatus.MISSED);
        }
        repository.saveAll(overdue);
    }
}
