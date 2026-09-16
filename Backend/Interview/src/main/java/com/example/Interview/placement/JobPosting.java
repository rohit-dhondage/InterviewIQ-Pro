package com.example.Interview.placement;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placement_drive_id", nullable = false)
    private PlacementDrive placementDrive;

    @Column(nullable = false)
    private String role; // e.g. "Software Engineer"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "package_ctc")
    private String packageCtc; // e.g. "12 LPA"

    @Column(name = "minimum_cgpa")
    private Double minimumCgpa;

    @Column(name = "required_skills")
    private String requiredSkills; // comma separated or JSON
}
