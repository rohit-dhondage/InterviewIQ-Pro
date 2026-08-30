package com.example.Interview.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateCompanyRequest(
        @NotBlank String name,
        List<String> requiredSkills,
        Double minimumScore,
        String difficulty
) {}
