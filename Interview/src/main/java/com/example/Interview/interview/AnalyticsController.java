package com.example.Interview.interview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("module", "analytics", "status", "scaffolded, Week 4");
    }
}