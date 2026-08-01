package com.example.Interview.student;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.college.College;
import com.example.Interview.college.Department;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private Integer year;

    @Column(name = "roll_no")
    private String rollNo;

    private Double cgpa;

    // Latest snapshot values — full history lives in the Progress table
    @Column(name = "resume_score")
    private Double resumeScore;

    @Column(name = "interview_score")
    private Double interviewScore;

    @Column(name = "readiness_score")
    private Double readinessScore;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "student_target_companies", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "company")
    private List<String> targetCompanies = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "student_preferred_topics", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "topic")
    private List<String> preferredStudyTopics = new ArrayList<>();
}
