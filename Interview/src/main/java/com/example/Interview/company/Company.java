package com.example.Interview.company;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "company_required_skills", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "skill")
    private List<String> requiredSkills = new ArrayList<>();

    @Column(name = "minimum_score")
    private Double minimumScore;

    private String difficulty;
}
