package com.example.Interview.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    List<Student> findByCollegeId(Long collegeId);
    List<Student> findByDepartmentId(Long departmentId);
}
