package com.example.Interview.tpo.dto;

import jakarta.validation.constraints.NotBlank;

public record JobPostingRequest(
        @NotBlank String role,
        String description,
        String packageCtc,
        Double minimumCgpa,
        String requiredSkills
) {}
