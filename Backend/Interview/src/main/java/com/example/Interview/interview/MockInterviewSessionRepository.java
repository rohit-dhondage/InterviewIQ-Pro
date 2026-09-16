package com.example.Interview.interview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MockInterviewSessionRepository extends JpaRepository<MockInterviewSession, String> {

    List<MockInterviewSession> findByStudentIdAndStatusOrderByScheduledAtAsc(
            Long studentId, MockInterviewSession.InterviewStatus status);

    List<MockInterviewSession> findByStudentIdOrderByScheduledAtDesc(Long studentId);

    List<MockInterviewSession> findByStatusAndScheduledAtBefore(
            MockInterviewSession.InterviewStatus status, LocalDateTime cutoff);
}
