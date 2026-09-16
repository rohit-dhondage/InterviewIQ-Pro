package com.example.Interview.placement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobPostingId(Long jobPostingId);
    List<JobApplication> findByStudentId(Long studentId);
    boolean existsByJobPostingIdAndStudentId(Long jobPostingId, Long studentId);
}
