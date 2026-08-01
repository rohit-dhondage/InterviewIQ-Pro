package com.example.Interview.roadmap;

import com.example.Interview.student.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roadmap_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "recommended_resource")
    private String recommendedResource;

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
}
