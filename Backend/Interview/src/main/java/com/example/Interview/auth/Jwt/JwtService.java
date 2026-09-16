package com.example.Interview.auth.Jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;

    public String generateToken(UserDetails userDetails) {
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    public String extractUsername(String token) {
        return jwtUtil.extractSubject(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = jwtUtil.extractSubject(token);
        return username.equals(userDetails.getUsername()) && !jwtUtil.isExpired(token);
    }
}