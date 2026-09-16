package com.example.Interview.dashboard;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.exception.ApiException;
import com.example.Interview.interview.MockInterviewSession;
import com.example.Interview.interview.MockInterviewSessionRepository;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StudentRepository studentRepository;
    private final MockInterviewSessionRepository interviewSessionRepository;

    @GetMapping("/me")
    public DashboardResponse me(@AuthenticationPrincipal User user) {
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Student profile not found", HttpStatus.NOT_FOUND));

        List<MockInterviewSession> upcomingInterviews = interviewSessionRepository
                .findByStudentIdAndStatusOrderByScheduledAtAsc(student.getId(), MockInterviewSession.InterviewStatus.SCHEDULED);

        return new DashboardResponse(
                student,
                student.getReadinessScore(),
                student.getResumeScore(),
                student.getInterviewScore(),
                upcomingInterviews
        );
    }

    public record DashboardResponse(
            Student profile,
            Double readinessScore,
            Double resumeScore,
            Double interviewScore,
            List<MockInterviewSession> upcomingInterviews
    ) {}
}