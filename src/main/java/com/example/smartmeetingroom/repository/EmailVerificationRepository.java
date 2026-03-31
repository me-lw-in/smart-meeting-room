package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.EmailVerification;
import com.example.smartmeetingroom.enums.EmailVerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<com.example.smartmeetingroom.entity.EmailVerification, Long> {
    boolean existsByToken(String token);

    Optional<EmailVerification> findByToken(String token);

    boolean existsByEmailAndTypeAndIsUsed(String email, EmailVerificationType type, Boolean isUsed);

    Optional<EmailVerification> findByEmailAndTypeAndIsUsed(String email, EmailVerificationType type, Boolean isUsed);
}
