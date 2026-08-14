package com.example.Interview.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        String targetRole, // optional

        @NotNull(message = "College is required")
        Long collegeId,

        @NotNull(message = "Department is required")
        Long departmentId,

        Integer year,       // optional, can be set later
        String rollNo        // optional, can be set later
) {}