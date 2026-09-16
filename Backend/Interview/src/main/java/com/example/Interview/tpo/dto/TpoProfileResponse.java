package com.example.Interview.tpo.dto;

import com.example.Interview.tpo.TpoProfile;

public record TpoProfileResponse(
        Long id,
        String fullName,
        String email,
        String contactNumber,
        Long collegeId,
        String collegeName
) {
    public static TpoProfileResponse from(TpoProfile p) {
        return new TpoProfileResponse(
                p.getId(),
                p.getUser().getFullName(),
                p.getUser().getEmail(),
                p.getContactNumber(),
                p.getCollege().getId(),
                p.getCollege().getName()
        );
    }
}
