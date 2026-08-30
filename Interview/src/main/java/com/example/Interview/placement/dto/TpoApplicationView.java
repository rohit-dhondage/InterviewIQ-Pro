package com.example.Interview.placement.dto;

import com.example.Interview.placement.JobApplication;
import com.example.Interview.placement.JobApplication.ApplicationStatus;
import java.time.LocalDateTime;

public record TpoApplicationView(
        Long applicationId,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        Long studentId,
        String studentName,
        String studentEmail,
        String rollNo,
        Double cgpa,
        Double readinessScore
) {
    public static TpoApplicationView from(JobApplication app) {
        var student = app.getStudent();
        return new TpoApplicationView(
                app.getId(),
                app.getStatus(),
                app.getAppliedAt(),
                student.getId(),
                student.getUser().getFullName(),
                student.getUser().getEmail(),
                student.getRollNo(),
                student.getCgpa(),
                student.getReadinessScore()
        );
    }
}
