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
        return Map.of("module", "analytics", "status", "scaffolded, Week 4");
    }

    @GetMapping("/me")
    public Map<String, Object> getMyAnalytics(@AuthenticationPrincipal User user) {
        List<Progress> history = progressService.getHistory(user);
        return Map.of(
            "history", history,
            "totalSessions", history.size()
        );
    }
}