package com.example.Interview.interview;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.progress.ProgressService;
import com.example.Interview.progress.Progress;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ProgressService progressService;

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("module", "analytics", "status", "active");
    }

    @GetMapping("/me")
    public Map<String, Object> getMyAnalytics(@AuthenticationPrincipal User user) {
        List<Progress> history = progressService.getHistory(user);
        int totalSessions = history.size();
        
        double avgScore = history.stream()
                .filter(p -> p.getReadinessScore() != null || p.getOverallScore() != null)
                .mapToDouble(p -> p.getReadinessScore() != null ? p.getReadinessScore() : p.getOverallScore())
                .average()
                .orElse(0.0);

        double techAvg = history.stream().filter(p -> p.getTechnicalScore() != null).mapToDouble(Progress::getTechnicalScore).average().orElse(0.0);
        double commAvg = history.stream().filter(p -> p.getCommunicationScore() != null).mapToDouble(Progress::getCommunicationScore).average().orElse(0.0);
        double sysAvg = history.stream().filter(p -> p.getConfidenceScore() != null).mapToDouble(Progress::getConfidenceScore).average().orElse(0.0);
        double coreAvg = history.stream().filter(p -> p.getGrammarScore() != null).mapToDouble(Progress::getGrammarScore).average().orElse(0.0);
        double resumeAvg = history.stream().filter(p -> p.getResumeScore() != null).mapToDouble(Progress::getResumeScore).average().orElse(0.0);

        Map<String, Integer> domainScores = Map.of(
            "Data Structures & Algorithms", (int) Math.round(techAvg),
            "Database & SQL Queries", (int) Math.round(techAvg > 0 ? (techAvg + commAvg) / 2 : 0),
            "System Design & Architecture", (int) Math.round(sysAvg),
            "Communication & Behavioral", (int) Math.round(commAvg),
            "Core CS (OS, Networking, OOP)", (int) Math.round(coreAvg),
            "Resume ATS Score", (int) Math.round(resumeAvg)
        );

        return Map.of(
            "overallReadiness", (int) Math.round(avgScore),
            "totalSessions", totalSessions,
            "domainScores", domainScores,
            "history", history
        );
    }
}