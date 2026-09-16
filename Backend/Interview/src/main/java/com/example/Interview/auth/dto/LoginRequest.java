package com.example.Interview.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email is Required")
        @Email(message = "Email must be valid")
        String email,
        @NotBlank(message = "Password is Required")
         String password
) {}
