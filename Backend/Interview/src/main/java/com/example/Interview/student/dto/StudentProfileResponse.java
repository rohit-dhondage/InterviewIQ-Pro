package com.example.Interview.student.dto;

import com.example.Interview.student.Student;
import java.util.List;

public record StudentProfileResponse(
        Long id,
        String fullName,
        String email,
        String targetRole,
        Long collegeId,
        String collegeName,
        Long departmentId,
        String departmentName,
        Integer year,
        String rollNo,
        Double cgpa,
        Double resumeScore,
        Double interviewScore,
        Double readinessScore,
        List<String> targetCompanies,
        List<String> preferredStudyTopics
) {
    public static StudentProfileResponse from(Student s) {
        return new StudentProfileResponse(
                s.getId(),
                s.getUser().getFullName(),
                s.getUser().getEmail(),
                s.getUser().getTargetRole(),
                s.getCollege().getId(),
                s.getCollege().getName(),
                s.getDepartment().getId(),
                s.getDepartment().getName(),
                s.getYear(),
                s.getRollNo(),
                s.getCgpa(),
                s.getResumeScore(),
                s.getInterviewScore(),
                s.getReadinessScore(),
                s.getTargetCompanies(),
                s.getPreferredStudyTopics()
        );
    }
}
