package com.example.Interview.tpo;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.college.College;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tpo_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TpoProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    private String contactNumber;
}
