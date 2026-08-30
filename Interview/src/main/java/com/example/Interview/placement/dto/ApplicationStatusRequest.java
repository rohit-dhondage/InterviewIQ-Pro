package com.example.Interview.placement.dto;

import com.example.Interview.placement.JobApplication.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(@NotNull ApplicationStatus status) {}
