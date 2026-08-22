package com.example.Interview.tpo;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.college.College;
import com.example.Interview.exception.ApiException;
import com.example.Interview.placement.PlacementDrive;
import com.example.Interview.placement.PlacementDriveRepository;
import com.example.Interview.placement.JobPosting;
import com.example.Interview.placement.JobPostingRepository;
import com.example.Interview.placement.JobApplication;
import com.example.Interview.placement.JobApplicationRepository;
import com.example.Interview.student.Student;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TpoService {

    private final TpoProfileRepository tpoProfileRepository;
    private final StudentRepository studentRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public List<Student> getStudentsInCollege(User user) {
        College college = resolveTpoCollege(user);
        return studentRepository.findByCollegeId(college.getId());
    }

    public List<PlacementDrive> getPlacementDrives(User user) {
        College college = resolveTpoCollege(user);
        return placementDriveRepository.findByCollegeIdOrderByStartDateDesc(college.getId());
    }

    public PlacementDrive createPlacementDrive(User user, PlacementDrive drive) {
        College college = resolveTpoCollege(user);
        drive.setCollege(college);
        return placementDriveRepository.save(drive);
    }

    public JobPosting addJobPosting(User user, Long driveId, JobPosting posting) {
        College college = resolveTpoCollege(user);
        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new ApiException("Placement drive not found", HttpStatus.NOT_FOUND));

        if (!drive.getCollege().getId().equals(college.getId())) {
            throw new ApiException("Not authorized to manage this drive", HttpStatus.FORBIDDEN);
        }

        posting.setPlacementDrive(drive);
        return jobPostingRepository.save(posting);
    }

    public List<JobPosting> getJobPostingsForDrive(User user, Long driveId) {
        // Simple auth check could be added
        return jobPostingRepository.findByPlacementDriveId(driveId);
    }

    public List<JobApplication> getApplicationsForJob(User user, Long jobId) {
        return jobApplicationRepository.findByJobPostingId(jobId);
    }

    private College resolveTpoCollege(User user) {
        return tpoProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("TPO Profile not found", HttpStatus.NOT_FOUND))
                .getCollege();
    }
}
