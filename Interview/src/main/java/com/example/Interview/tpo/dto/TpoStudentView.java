package com.example.Interview.tpo.dto;

import com.example.Interview.student.Student;

public record TpoStudentView(
        Long studentId,
        String fullName,
        String email,
        String rollNo,
        Integer year,
        String departmentName,
        Double cgpa,
        Double resumeScore,
        Double interviewScore,
        Double readinessScore
) {
    public static TpoStudentView from(Student s) {
        return new TpoStudentView(
                s.getId(),
                s.getUser().getFullName(),
                s.getUser().getEmail(),
                s.getRollNo(),
                s.getYear(),
                s.getDepartment().getName(),
                s.getCgpa(),
                s.getResumeScore(),
                s.getInterviewScore(),
                s.getReadinessScore()
        );
    }
}
