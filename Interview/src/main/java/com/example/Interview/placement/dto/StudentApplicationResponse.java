package com.example.Interview.placement.dto;

import com.example.Interview.placement.JobApplication;
import com.example.Interview.placement.JobApplication.ApplicationStatus;
import java.time.LocalDateTime;

public record StudentApplicationResponse(
        Long applicationId,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        Long jobId,
        String jobRole,
        String packageCtc,
        Double minimumCgpa,
        String companyName,
        String driveName,
        LocalDateTime driveStartDate,
        LocalDateTime driveEndDate
) {
    public static StudentApplicationResponse from(JobApplication app) {
        var job   = app.getJobPosting();
        var drive = job.getPlacementDrive();
        return new StudentApplicationResponse(
                app.getId(),
                app.getStatus(),
                app.getAppliedAt(),
                job.getId(),
                job.getRole(),
                job.getPackageCtc(),
                job.getMinimumCgpa(),
                drive.getCompany().getName(),
                drive.getName(),
                drive.getStartDate(),
                drive.getEndDate()
        );
    }
}
