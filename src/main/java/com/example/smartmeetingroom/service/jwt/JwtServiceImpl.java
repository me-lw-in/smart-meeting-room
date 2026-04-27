package com.example.smartmeetingroom.service.jwt;

import com.example.smartmeetingroom.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@AllArgsConstructor
public class JwtServiceImpl implements JwtService{

    private final UserRepository userRepository;

    private final static String secretKey = "0pKLJWqntzOHn4gXGjPqjVoxCQ2cZGT8SAvmRwfxWnxeG67eB1mhcn";
    private final static long expireTime = 86000;

    @Override
    public String generateToken(String email){
        var user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
        return Jwts.builder()
                .subject(email)
                .claim("role", user.getRoles().getRoleName())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * expireTime))
                .signWith(getSigningKey())
                .compact();

    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    @Override
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    @Override
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

}
