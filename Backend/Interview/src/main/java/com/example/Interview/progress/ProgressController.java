package com.example.Interview.progress;

import com.example.Interview.auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress/me")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public List<Progress> getHistory(@AuthenticationPrincipal User user) {
        return progressService.getHistory(user);
    }

    @GetMapping("/latest")
    public Progress getLatest(@AuthenticationPrincipal User user) {
        return progressService.getLatest(user);
    }
}
