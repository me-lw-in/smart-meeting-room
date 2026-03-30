package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<com.example.smartmeetingroom.entity.EmailVerification, Long> {
    boolean existsByToken(String token);

    Optional<EmailVerification> findByToken(String token);

    Optional<EmailVerification> findByEmailAndIsVerified(String email, Boolean isVerified);

    boolean existsByEmailAndIsVerified(String email, Boolean isVerified);
}
