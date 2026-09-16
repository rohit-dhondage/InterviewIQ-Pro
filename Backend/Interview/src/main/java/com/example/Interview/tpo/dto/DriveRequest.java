package com.example.Interview.tpo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record DriveRequest(
        @NotNull Long companyId,
        @NotBlank String name,
        String description,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate
) {}
