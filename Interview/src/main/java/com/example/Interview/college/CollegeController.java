package com.example.Interview.college;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/colleges")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    @GetMapping
    public List<College> getAllColleges() {
        return collegeService.getAllColleges();
    }

    @GetMapping("/{collegeId}/departments")
    public List<Department> getDepartments(@PathVariable Long collegeId) {
        return collegeService.getDepartments(collegeId);
    }
}