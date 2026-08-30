package com.example.Interview.student;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.student.dto.StudentProfileResponse;
import com.example.Interview.student.dto.TpoContactResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students/me")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentService studentService;

    @GetMapping("/profile")
    public StudentProfileResponse getProfile(@AuthenticationPrincipal User user) {
        return studentService.getStudentProfile(user);
    }

    @PutMapping("/profile")
    public StudentProfileResponse updateProfile(@AuthenticationPrincipal User user, @RequestBody StudentService.UpdateProfileRequest request) {
        return studentService.updateStudentProfile(user, request);
    }

    @GetMapping("/tpo-contact")
    public List<TpoContactResponse> getTpoContact(@AuthenticationPrincipal User user) {
        return studentService.getTpoContacts(user);
    }
}
