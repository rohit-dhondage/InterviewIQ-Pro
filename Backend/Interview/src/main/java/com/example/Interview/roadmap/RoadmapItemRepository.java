package com.example.Interview.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, Long> {

    List<RoadmapItem> findByStudentIdOrderByIdAsc(Long studentId);

    Optional<RoadmapItem> findByIdAndStudentId(Long id, Long studentId);

    void deleteByStudentId(Long studentId);
}
