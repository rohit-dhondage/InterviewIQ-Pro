package com.example.Interview.progress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    List<Progress> findByStudentIdOrderByRecordedAtDesc(Long studentId);

    Optional<Progress> findFirstByStudentIdOrderByRecordedAtDesc(Long studentId);

    @Query("SELECT AVG(p.resumeScore) FROM Progress p WHERE p.student.id = :studentId AND p.resumeScore IS NOT NULL")
    Double avgResumeScore(Long studentId);

    @Query("SELECT AVG(p.overallScore) FROM Progress p WHERE p.student.id = :studentId AND p.overallScore IS NOT NULL")
    Double avgOverallScore(Long studentId);
}
