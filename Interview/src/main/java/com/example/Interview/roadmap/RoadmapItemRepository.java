package com.example.Interview.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, Long> {
    List<RoadmapItem> findByStudentId(Long studentId);
}
