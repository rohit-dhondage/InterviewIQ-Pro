package com.example.Interview.student;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students/{studentId}/preferences")
@RequiredArgsConstructor
public class StudentPreferencesController {

    private final StudentRepository studentRepository;

    @GetMapping
    public PreferencesResponse get(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No student found for id: " + studentId));
        return new PreferencesResponse(student.getTargetCompanies(), student.getPreferredStudyTopics());
    }

    @PutMapping("/target-companies")
    public PreferencesResponse updateTargetCompanies(@PathVariable Long studentId, @RequestBody List<String> companies) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No student found for id: " + studentId));
        student.setTargetCompanies(companies);
        studentRepository.save(student);
        return new PreferencesResponse(student.getTargetCompanies(), student.getPreferredStudyTopics());
    }

    @PutMapping("/study-topics")
    public PreferencesResponse updateStudyTopics(@PathVariable Long studentId, @RequestBody List<String> topics) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No student found for id: " + studentId));
        student.setPreferredStudyTopics(topics);
        studentRepository.save(student);
        return new PreferencesResponse(student.getTargetCompanies(), student.getPreferredStudyTopics());
    }

    public record PreferencesResponse(List<String> targetCompanies, List<String> preferredStudyTopics) {}
}
