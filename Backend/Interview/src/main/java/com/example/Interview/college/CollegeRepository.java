package com.example.Interview.college;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {
    boolean existsByName(String name);
    java.util.Optional<College> findByName(String name);
}
