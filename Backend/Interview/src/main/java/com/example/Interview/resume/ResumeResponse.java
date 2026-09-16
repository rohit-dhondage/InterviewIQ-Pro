package com.example.Interview.resume;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        Double atsScore,
        String feedback,
        LocalDateTime uploadedAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getAtsScore(),
                resume.getFeedback(),
                resume.getUploadedAt()
        );
    }
}