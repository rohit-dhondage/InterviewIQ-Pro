package com.example.Interview.tpo;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.placement.PlacementDrive;
import com.example.Interview.placement.JobPosting;
import com.example.Interview.placement.JobApplication;
import com.example.Interview.student.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tpo")
@RequiredArgsConstructor
public class TpoController {

    private final TpoService tpoService;

    @GetMapping("/students")
    public List<Student> getStudents(@AuthenticationPrincipal User user) {
        return tpoService.getStudentsInCollege(user);
    }

    @GetMapping("/drives")
    public List<PlacementDrive> getDrives(@AuthenticationPrincipal User user) {
        return tpoService.getPlacementDrives(user);
    }

    @PostMapping("/drives")
    public PlacementDrive createDrive(@AuthenticationPrincipal User user, @RequestBody PlacementDrive drive) {
        return tpoService.createPlacementDrive(user, drive);
    }

    @GetMapping("/drives/{driveId}/jobs")
    public List<JobPosting> getJobs(@AuthenticationPrincipal User user, @PathVariable Long driveId) {
        return tpoService.getJobPostingsForDrive(user, driveId);
    }

    @PostMapping("/drives/{driveId}/jobs")
    public JobPosting addJob(@AuthenticationPrincipal User user, @PathVariable Long driveId, @RequestBody JobPosting job) {
        return tpoService.addJobPosting(user, driveId, job);
    }

    @GetMapping("/jobs/{jobId}/applications")
    public List<JobApplication> getApplications(@AuthenticationPrincipal User user, @PathVariable Long jobId) {
        return tpoService.getApplicationsForJob(user, jobId);
    }
}
