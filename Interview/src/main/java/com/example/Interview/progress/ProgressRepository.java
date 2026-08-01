package com.example.Interview.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByStudentIdOrderByRecordedAtAsc(Long studentId);
    Progress findTopByStudentIdOrderByRecordedAtDesc(Long studentId);
}
