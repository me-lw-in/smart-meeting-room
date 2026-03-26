package com.example.smartmeetingroom.service.jwt;

public interface JwtService {

    public String generateToken(String email);

    public String extractEmail(String token);

    public Long extractUserId(String token);

    public String extractRole(String token);
}
