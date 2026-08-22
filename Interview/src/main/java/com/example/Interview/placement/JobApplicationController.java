package com.example.Interview.placement;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.exception.ApiException;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
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
        // Validation that drive belongs to student's college
        Student student = resolveStudent(user);
        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new ApiException("Drive not found", HttpStatus.NOT_FOUND));

        if (!drive.getCollege().getId().equals(student.getCollege().getId())) {
            throw new ApiException("Not authorized to view this drive", HttpStatus.FORBIDDEN);
        }

        return jobPostingRepository.findByPlacementDriveId(driveId);
    }

    @GetMapping("/applications")
    public List<JobApplication> getMyApplications(@AuthenticationPrincipal User user) {
        Student student = resolveStudent(user);
        return jobApplicationRepository.findByStudentId(student.getId());
    }

    @PostMapping("/{jobId}/apply")
    public JobApplication applyToJob(@AuthenticationPrincipal User user, @PathVariable Long jobId) {
        Student student = resolveStudent(user);
        
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job posting not found", HttpStatus.NOT_FOUND));

        if (!job.getPlacementDrive().getCollege().getId().equals(student.getCollege().getId())) {
            throw new ApiException("Not authorized to apply to this job", HttpStatus.FORBIDDEN);
        }

        if (jobApplicationRepository.existsByJobPostingIdAndStudentId(jobId, student.getId())) {
            throw new ApiException("Already applied to this job", HttpStatus.CONFLICT);
        }

        JobApplication application = JobApplication.builder()
                .student(student)
                .jobPosting(job)
                .status(JobApplication.ApplicationStatus.APPLIED)
                .build();

        return jobApplicationRepository.save(application);
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Student profile not found", HttpStatus.NOT_FOUND));
    }
}
