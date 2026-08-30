package com.example.Interview.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDepartmentRequest(@NotBlank String name) {}
