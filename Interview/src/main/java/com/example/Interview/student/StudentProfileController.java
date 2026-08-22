package com.example.Interview.student;

import com.example.Interview.auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students/me/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentService studentService;

    @GetMapping
    public Student getProfile(@AuthenticationPrincipal User user) {
        return studentService.getStudentProfile(user);
    }

    @PutMapping
    public Student updateProfile(@AuthenticationPrincipal User user, @RequestBody StudentService.UpdateProfileRequest request) {
        return studentService.updateStudentProfile(user, request);
    }
}
