package com.example.Interview.config;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.auth.Repository.UserRepository;
import com.example.Interview.auth.Role;
import com.example.Interview.college.College;
import com.example.Interview.college.CollegeRepository;
import com.example.Interview.college.Department;
import com.example.Interview.college.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the platform Admin and top Premier Colleges of Nashik on startup.
 * Admin Credentials: admin@interviewiq.com / Admin@123456
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL    = "admin@interviewiq.com";
    private static final String ADMIN_PASSWORD = "Admin@123456";
    private static final String ADMIN_NAME     = "Platform Admin";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            User admin = User.builder()
                    .fullName(ADMIN_NAME)
                    .email(ADMIN_EMAIL)
                    .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("=== ADMIN SEEDED: {} | default password: {} ===", ADMIN_EMAIL, ADMIN_PASSWORD);
        }

        // Ensure all 12 Nashik Colleges exist in Database
        List<String[]> nashikColleges = List.of(
            new String[]{"MET's Institute of Engineering, Bhujbal Knowledge City (MET BKC), Nashik", "Adgaon, Nashik"},
            new String[]{"K. K. Wagh Institute of Engineering Education & Research (KKWIEER), Nashik", "Amrutdham, Panchavati, Nashik"},
            new String[]{"PVG's College of Engineering & S. S. Dhamankar Institute of Management (PVGCOE), Nashik", "Mhasrul, Dindori Road, Nashik"},
            new String[]{"NDMVP Samaj's KBT College of Engineering (KBTCOE), Nashik", "Gangapur Road, Nashik"},
            new String[]{"Sandip Institute of Technology & Research Centre (SITRC), Nashik", "Trimbak Road, Mahiravani, Nashik"},
            new String[]{"Guru Gobind Singh College of Engineering & Research Centre (GCOERC), Nashik", "Pathardi Phata, Nashik"},
            new String[]{"Matoshri College of Engineering & Research Centre (MCOERC), Nashik", "Eklahare, Near Odha, Nashik"},
            new String[]{"GES's R. H. Sapat College of Engineering, Management & Research, Nashik", "Prashant Nagar, Nashik"},
            new String[]{"Sir Visvesvaraya Institute of Technology (SVIT), Chincholi, Nashik", "Chincholi, Sinnar, Nashik"},
            new String[]{"Brahma Valley College of Engineering & Research Centre, Nashik", "Anjaneri, Trimbakeshwar, Nashik"},
            new String[]{"Gokhale Education Society's JDC Bytco Institute of Management, Nashik", "College Road, Nashik"},
            new String[]{"SNJB's Late Sau KB Jain College of Engineering, Chandwad, Nashik", "Neminagar, Chandwad, Nashik"}
        );

        List<String> departments = List.of(
            "Computer Engineering",
            "Information Technology",
            "Artificial Intelligence & Data Science (AI & DS)",
            "Electronics & Telecommunication (E&TC)",
            "Mechanical Engineering"
        );

        for (String[] cData : nashikColleges) {
            String colName = cData[0];
            if (!collegeRepository.existsByName(colName)) {
                College savedCollege = collegeRepository.save(
                    College.builder()
                        .name(colName)
                        .address(cData[1])
                        .build()
                );

                for (String deptName : departments) {
                    departmentRepository.save(
                        Department.builder()
                            .name(deptName)
                            .college(savedCollege)
                            .build()
                    );
                }
                log.info("Seeded college: {}", colName);
            }
        }
        log.info("=== NASHIK COLLEGES AND DEPARTMENTS SYNCHRONIZED ===");
    }
}
