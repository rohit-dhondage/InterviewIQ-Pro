package com.example.Interview.tpo;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.college.College;
import com.example.Interview.company.Company;
import com.example.Interview.company.CompanyRepository;
import com.example.Interview.exception.ApiException;
import com.example.Interview.placement.PlacementDrive;
import com.example.Interview.placement.PlacementDriveRepository;
import com.example.Interview.placement.JobPosting;
import com.example.Interview.placement.JobPostingRepository;
import com.example.Interview.placement.JobApplication;
import com.example.Interview.placement.JobApplicationRepository;
import com.example.Interview.student.StudentRepository;
import com.example.Interview.tpo.dto.*;
import com.example.Interview.placement.dto.TpoApplicationView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TpoService {

    private final TpoProfileRepository tpoProfileRepository;
    private final StudentRepository studentRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyRepository companyRepository;

    public TpoProfileResponse getProfile(User user) {
        TpoProfile profile = resolveProfile(user);
        return TpoProfileResponse.from(profile);
    }

    public List<TpoStudentView> getStudentsInCollege(User user) {
        College college = resolveTpoCollege(user);
        return studentRepository.findByCollegeId(college.getId()).stream()
                .map(TpoStudentView::from)
                .toList();
    }

    public List<PlacementDrive> getPlacementDrives(User user) {
        College college = resolveTpoCollege(user);
        return placementDriveRepository.findByCollegeIdOrderByStartDateDesc(college.getId());
    }

    @Transactional
    public PlacementDrive createPlacementDrive(User user, DriveRequest req) {
        College college = resolveTpoCollege(user);
        Company company = companyRepository.findById(req.companyId())
                .orElseThrow(() -> new ApiException("Company not found", HttpStatus.NOT_FOUND));

        PlacementDrive drive = PlacementDrive.builder()
                .college(college)
                .company(company)
                .name(req.name())
                .description(req.description())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .isActive(true)
                .build();
        return placementDriveRepository.save(drive);
    }

    @Transactional
    public PlacementDrive updatePlacementDrive(User user, Long driveId, DriveRequest req) {
        PlacementDrive drive = resolveDriveAuth(user, driveId);
        Company company = companyRepository.findById(req.companyId())
                .orElseThrow(() -> new ApiException("Company not found", HttpStatus.NOT_FOUND));

        drive.setCompany(company);
        drive.setName(req.name());
        drive.setDescription(req.description());
        drive.setStartDate(req.startDate());
        drive.setEndDate(req.endDate());
        return placementDriveRepository.save(drive);
    }

    @Transactional
    public void deletePlacementDrive(User user, Long driveId) {
        PlacementDrive drive = resolveDriveAuth(user, driveId);
        placementDriveRepository.delete(drive);
    }

    @Transactional
    public JobPosting addJobPosting(User user, Long driveId, JobPostingRequest req) {
        PlacementDrive drive = resolveDriveAuth(user, driveId);

        JobPosting posting = JobPosting.builder()
                .placementDrive(drive)
                .role(req.role())
                .description(req.description())
                .packageCtc(req.packageCtc())
                .minimumCgpa(req.minimumCgpa())
                .requiredSkills(req.requiredSkills())
                .build();
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting updateJobPosting(User user, Long jobId, JobPostingRequest req) {
        JobPosting job = resolveJobAuth(user, jobId);
        job.setRole(req.role());
        job.setDescription(req.description());
        job.setPackageCtc(req.packageCtc());
        job.setMinimumCgpa(req.minimumCgpa());
        job.setRequiredSkills(req.requiredSkills());
        return jobPostingRepository.save(job);
    }

    @Transactional
    public void deleteJobPosting(User user, Long jobId) {
        JobPosting job = resolveJobAuth(user, jobId);
        jobPostingRepository.delete(job);
    }

    public List<JobPosting> getJobPostingsForDrive(User user, Long driveId) {
        resolveDriveAuth(user, driveId); // verify ownership
        return jobPostingRepository.findByPlacementDriveId(driveId);
    }

    public List<TpoApplicationView> getApplicationsForJob(User user, Long jobId) {
        resolveJobAuth(user, jobId); // verify ownership
        return jobApplicationRepository.findByJobPostingId(jobId).stream()
                .map(TpoApplicationView::from)
                .toList();
    }

    @Transactional
    public TpoApplicationView updateApplicationStatus(User user, Long appId, JobApplication.ApplicationStatus status) {
        JobApplication app = jobApplicationRepository.findById(appId)
                .orElseThrow(() -> new ApiException("Application not found", HttpStatus.NOT_FOUND));
        
        // Verify TPO owns the drive this application belongs to
        resolveDriveAuth(user, app.getJobPosting().getPlacementDrive().getId());
        
        app.setStatus(status);
        app = jobApplicationRepository.save(app);
        return TpoApplicationView.from(app);
    }

    private TpoProfile resolveProfile(User user) {
        return tpoProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("TPO Profile not found", HttpStatus.NOT_FOUND));
    }

    private College resolveTpoCollege(User user) {
        return resolveProfile(user).getCollege();
    }

    private PlacementDrive resolveDriveAuth(User user, Long driveId) {
        College college = resolveTpoCollege(user);
        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new ApiException("Placement drive not found", HttpStatus.NOT_FOUND));

        if (!drive.getCollege().getId().equals(college.getId())) {
            throw new ApiException("Not authorized to manage this drive", HttpStatus.FORBIDDEN);
        }
        return drive;
    }

    private JobPosting resolveJobAuth(User user, Long jobId) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job posting not found", HttpStatus.NOT_FOUND));
        resolveDriveAuth(user, job.getPlacementDrive().getId());
        return job;
    }
}
