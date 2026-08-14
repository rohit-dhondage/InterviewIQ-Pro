package com.example.Interview.resume;

import com.example.Interview.auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/me", consumes = "multipart/form-data")
    public ResponseEntity<ResumeResponse> upload(@AuthenticationPrincipal User user,
                                                 @RequestParam("file") MultipartFile file) {
        Resume saved = resumeService.upload(user, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResumeResponse.from(saved));
    }

    @GetMapping("/me")
    public List<ResumeResponse> history(@AuthenticationPrincipal User user) {
        return resumeService.history(user).stream().map(ResumeResponse::from).toList();
    }

    @GetMapping("/me/latest")
    public ResumeResponse latest(@AuthenticationPrincipal User user) {
        return ResumeResponse.from(resumeService.latest(user));
    }
}