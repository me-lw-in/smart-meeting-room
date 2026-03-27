package com.example.smartmeetingroom.service.jwt;

import io.jsonwebtoken.Claims;

public interface JwtService {

    public Claims extractAllClaims(String token);

    public String generateToken(String email);

    public String extractEmail(String token);

    public Long extractUserId(String token);

    public String extractRole(String token);
}
