package com.example.Interview.placement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, Long> {
    List<PlacementDrive> findByCollegeIdOrderByStartDateDesc(Long collegeId);
}
