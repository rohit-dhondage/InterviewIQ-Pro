package com.example.Interview.admin.dto;

public record PlatformAnalyticsResponse(
        long totalStudents,
        long totalColleges,
        long totalTpos,
        long totalInterviews,
        long completedInterviews,
        long totalApplications,
        Double averageResumeScore,
        Double averageInterviewScore
) {}
