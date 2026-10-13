package com.example.Interview.student;

import com.example.Interview.auth.Entity.User;
import com.example.Interview.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/students/me/preferences")
@RequiredArgsConstructor
public class StudentPreferencesController {

    private final StudentRepository studentRepository;

    @GetMapping
    public PreferencesResponse get(@AuthenticationPrincipal User user) {
        Student student = resolveStudent(user);
        return new PreferencesResponse(student.getTargetCompanies(), student.getPreferredStudyTopics());
    }

    @PutMapping("/target-companies")
    public PreferencesResponse updateTargetCompanies(@AuthenticationPrincipal User user, @RequestBody JsonNode body) {
        List<String> companies = new ArrayList<>();
        if (body.isArray()) {
            body.forEach(item -> companies.add(item.asText()));
        } else if (body.has("targetCompanies") && body.get("targetCompanies").isArray()) {
            body.get("targetCompanies").forEach(item -> companies.add(item.asText()));
        } else if (body.isTextual()) {
            companies.add(body.asText());
        }

        Student student = resolveStudent(user);
        student.setTargetCompanies(companies);
        studentRepository.save(student);
        return new PreferencesResponse(student.getTargetCompanies(), student.getPreferredStudyTopics());
    }

    @PutMapping("/study-topics")
    public PreferencesResponse updateStudyTopics(@AuthenticationPrincipal User user, @RequestBody JsonNode body) {
        List<String> topics = new ArrayList<>();
        if (body.isArray()) {
            body.forEach(item -> topics.add(item.asText()));
        } else if (body.has("preferredStudyTopics") && body.get("preferredStudyTopics").isArray()) {
            body.get("preferredStudyTopics").forEach(item -> topics.add(item.asText()));
        } else if (body.has("studyTopics") && body.get("studyTopics").isArray()) {
            body.get("studyTopics").forEach(item -> topics.add(item.asText()));
        } else if (body.isTextual()) {
            topics.add(body.asText());
        }

        Student student = resolveStudent(user);
        student.setPreferredStudyTopics(topics);
        studentRepository.save(student);
        return new PreferencesResponse(student.getTargetCompanies(), student.getPreferredStudyTopics());
    }

    private Student resolveStudent(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("No student profile found for this account", HttpStatus.NOT_FOUND));
    }

    public record PreferencesResponse(List<String> targetCompanies, List<String> preferredStudyTopics) {}
}