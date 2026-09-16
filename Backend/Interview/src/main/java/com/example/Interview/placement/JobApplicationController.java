package com.example.Interview.placement;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.exception.ApiException;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import com.example.Interview.placement.dto.StudentApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students/me/jobs")
@RequiredArgsConstructor
public class JobApplicationController {

    private final StudentRepository studentRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @GetMapping("/drives")
    public List<PlacementDrive> getAvailableDrives(@AuthenticationPrincipal User user) {
        Student student = resolveStudent(user);
        return placementDriveRepository.findByCollegeIdOrderByStartDateDesc(student.getCollege().getId());
    }

    @GetMapping("/drives/{driveId}/jobs")
    public List<JobPosting> getJobsForDrive(@AuthenticationPrincipal User user, @PathVariable Long driveId) {
        Student student = resolveStudent(user);
        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new ApiException("Drive not found", HttpStatus.NOT_FOUND));

        if (!drive.getCollege().getId().equals(student.getCollege().getId())) {
            throw new ApiException("Not authorized to view this drive", HttpStatus.FORBIDDEN);
        }

        return jobPostingRepository.findByPlacementDriveId(driveId);
    }

    @GetMapping("/applications")
    public List<StudentApplicationResponse> getMyApplications(@AuthenticationPrincipal User user) {
        Student student = resolveStudent(user);
        return jobApplicationRepository.findByStudentId(student.getId()).stream()
                .map(StudentApplicationResponse::from)
                .toList();
    }

    @PostMapping("/{jobId}/apply")
    public StudentApplicationResponse applyToJob(@AuthenticationPrincipal User user, @PathVariable Long jobId) {
        Student student = resolveStudent(user);
        
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job posting not found", HttpStatus.NOT_FOUND));

        PlacementDrive drive = job.getPlacementDrive();

        if (!drive.getCollege().getId().equals(student.getCollege().getId())) {
            throw new ApiException("Not authorized to apply to this job", HttpStatus.FORBIDDEN);
        }

        if (drive.getIsActive() != null && !drive.getIsActive()) {
            throw new ApiException("This placement drive is no longer active", HttpStatus.BAD_REQUEST);
        }

        if (job.getMinimumCgpa() != null && (student.getCgpa() == null || student.getCgpa() < job.getMinimumCgpa())) {
            throw new ApiException("You do not meet the minimum CGPA criteria for this role", HttpStatus.BAD_REQUEST);
        }

        if (jobApplicationRepository.existsByJobPostingIdAndStudentId(jobId, student.getId())) {
            throw new ApiException("Already applied to this job", HttpStatus.CONFLICT);
        }

        JobApplication application = JobApplication.builder()
                .student(student)
                .jobPosting(job)
                .status(JobApplication.ApplicationStatus.APPLIED)
                .build();

        application = jobApplicationRepository.save(application);
        return StudentApplicationResponse.from(application);
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Student profile not found", HttpStatus.NOT_FOUND));
    }
}
