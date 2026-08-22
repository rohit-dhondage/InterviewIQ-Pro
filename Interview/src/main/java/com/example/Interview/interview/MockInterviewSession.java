package com.example.Interview.interview;

import com.example.Interview.student.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mock_interview_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private String company;
    private String role;
    private String round;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type")
    private InterviewType interviewType; // HR or TECHNICAL

    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private Integer durationSeconds;

    @Column(name = "current_question_count")
    @Builder.Default
    private Integer currentQuestionCount = 0;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String transcript;

    public enum InterviewStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, MISSED
    }

    public enum InterviewType {
        HR, TECHNICAL
    }
}
