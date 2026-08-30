package com.example.Interview.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCollegeRequest(
        @NotBlank String name,
        String address
) {}
