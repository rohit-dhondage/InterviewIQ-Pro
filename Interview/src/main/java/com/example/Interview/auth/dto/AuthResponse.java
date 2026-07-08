package com.example.Interview.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String fullName,
        String email
) {}