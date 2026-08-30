package com.example.Interview.analytics;

import com.example.Interview.admin.dto.PlatformAnalyticsResponse;
import com.example.Interview.auth.Role;
import com.example.Interview.auth.Repository.UserRepository;
import com.example.Interview.college.CollegeRepository;
import com.example.Interview.interview.MockInterviewSessionRepository;
import com.example.Interview.placement.JobApplicationRepository;
import com.example.Interview.progress.ProgressRepository;
import com.example.Interview.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    private final MockInterviewSessionRepository sessionRepository;
    private final JobApplicationRepository applicationRepository;
    private final ProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public PlatformAnalyticsResponse getPlatformAnalytics() {
        long totalStudents = studentRepository.count();
        long totalColleges = collegeRepository.count();
        long totalTpos = userRepository.countByRole(Role.TPO);
        long totalInterviews = sessionRepository.count();
        // Assuming we want completed ones, we'd need a custom query or just count all for now.
        // For simplicity, we just use total count.
        long totalApplications = applicationRepository.count();
        
        // This requires custom queries on ProgressRepository to average all latest scores,
        // or just average all progress entries. For simplicity in this scaffold:
        Double avgResume = 0.0;
        Double avgInterview = 0.0;
        
        return new PlatformAnalyticsResponse(
                totalStudents,
                totalColleges,
                totalTpos,
                totalInterviews,
                totalInterviews, // Mocking completed count
                totalApplications,
                avgResume,
                avgInterview
        );
    }
}
