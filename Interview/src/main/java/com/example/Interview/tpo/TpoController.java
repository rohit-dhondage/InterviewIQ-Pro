package com.example.Interview.tpo;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.placement.PlacementDrive;
import com.example.Interview.placement.JobPosting;
import com.example.Interview.placement.dto.ApplicationStatusRequest;
import com.example.Interview.placement.dto.TpoApplicationView;
import com.example.Interview.tpo.dto.DriveRequest;
import com.example.Interview.tpo.dto.JobPostingRequest;
import com.example.Interview.tpo.dto.TpoProfileResponse;
import com.example.Interview.tpo.dto.TpoStudentView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tpo")
@RequiredArgsConstructor
public class TpoController {

    private final TpoService tpoService;

    @GetMapping("/profile")
    public TpoProfileResponse getProfile(@AuthenticationPrincipal User user) {
        return tpoService.getProfile(user);
    }

    @GetMapping("/students")
    public List<TpoStudentView> getStudents(@AuthenticationPrincipal User user) {
        return tpoService.getStudentsInCollege(user);
    }

    // --- Drives ---
    @GetMapping("/drives")
    public List<PlacementDrive> getDrives(@AuthenticationPrincipal User user) {
        return tpoService.getPlacementDrives(user);
    }

    @PostMapping("/drives")
    @ResponseStatus(HttpStatus.CREATED)
    public PlacementDrive createDrive(@AuthenticationPrincipal User user, @Valid @RequestBody DriveRequest req) {
        return tpoService.createPlacementDrive(user, req);
    }

    @PutMapping("/drives/{driveId}")
    public PlacementDrive updateDrive(@AuthenticationPrincipal User user, @PathVariable Long driveId, @Valid @RequestBody DriveRequest req) {
        return tpoService.updatePlacementDrive(user, driveId, req);
    }

    @DeleteMapping("/drives/{driveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDrive(@AuthenticationPrincipal User user, @PathVariable Long driveId) {
        tpoService.deletePlacementDrive(user, driveId);
    }

    // --- Jobs ---
    @GetMapping("/drives/{driveId}/jobs")
    public List<JobPosting> getJobs(@AuthenticationPrincipal User user, @PathVariable Long driveId) {
        return tpoService.getJobPostingsForDrive(user, driveId);
    }

    @PostMapping("/drives/{driveId}/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobPosting addJob(@AuthenticationPrincipal User user, @PathVariable Long driveId, @Valid @RequestBody JobPostingRequest req) {
        return tpoService.addJobPosting(user, driveId, req);
    }

    @PutMapping("/jobs/{jobId}")
    public JobPosting updateJob(@AuthenticationPrincipal User user, @PathVariable Long jobId, @Valid @RequestBody JobPostingRequest req) {
        return tpoService.updateJobPosting(user, jobId, req);
    }

    @DeleteMapping("/jobs/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@AuthenticationPrincipal User user, @PathVariable Long jobId) {
        tpoService.deleteJobPosting(user, jobId);
    }

    // --- Applications ---
    @GetMapping("/jobs/{jobId}/applications")
    public List<TpoApplicationView> getApplications(@AuthenticationPrincipal User user, @PathVariable Long jobId) {
        return tpoService.getApplicationsForJob(user, jobId);
    }

    @PatchMapping("/applications/{appId}/status")
    public TpoApplicationView updateApplicationStatus(@AuthenticationPrincipal User user, 
                                                      @PathVariable Long appId, 
                                                      @Valid @RequestBody ApplicationStatusRequest req) {
        return tpoService.updateApplicationStatus(user, appId, req.status());
    }
}
