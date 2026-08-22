package com.example.Interview.tpo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TpoProfileRepository extends JpaRepository<TpoProfile, Long> {
    Optional<TpoProfile> findByUserId(Long userId);
}
