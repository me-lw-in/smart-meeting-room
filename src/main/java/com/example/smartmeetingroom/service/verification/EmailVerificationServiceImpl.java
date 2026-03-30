package com.example.smartmeetingroom.service.verification;

import com.example.smartmeetingroom.dto.user.EmailDTO;
import com.example.smartmeetingroom.entity.EmailVerification;
import com.example.smartmeetingroom.enums.EmailVerificationType;
import com.example.smartmeetingroom.repository.EmailVerificationRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.service.email.EmailService;
import com.example.smartmeetingroom.service.user.UserService;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService{

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional
    public void resendEmailForVerification(EmailDTO dto) {
        String email = dto.getEmail().toLowerCase().trim();
        var verificationObj = emailVerificationRepository.findByEmailAndIsVerified(email, false).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.CONFLICT, "Cannot resend verification link.")
        );
        String newToken = generateVerificationToken();
        // set expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedTime = expirationTime.format(formatter);

        verificationObj.setToken(newToken);
        verificationObj.setExpiryTime(expirationTime);

        emailService.sendEmail(email, newToken, formattedTime);
    }

    public void createEmailForVerification(EmailDTO dto){
        String email = dto.getEmail().toLowerCase().trim();
        Long userId = SecurityUtil.getCurrentUserId();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }
        if (emailVerificationRepository.existsByEmailAndIsVerified(email, false)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Verification link has already sent to this email.");
        }

        var token = generateVerificationToken();

        // set expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedTime = expirationTime.format(formatter);

        var emailVerification = new EmailVerification();
        emailVerification.setEmail(email);
        emailVerification.setToken(token);
        emailVerification.setType(EmailVerificationType.EMAIL_CHANGE);
        emailVerification.setExpiryTime(expirationTime);
        emailVerification.setUser(userRepository.getReferenceById(userId));
        emailVerificationRepository.save(emailVerification);

        emailService.sendEmail(email, token, formattedTime);
    }

    private String generateVerificationToken(){
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (emailVerificationRepository.existsByToken(token));

        return token;
    }

    public void verifyToken(String token) {
        var verificationObj = emailVerificationRepository.findByToken(token).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token!")
        );
        if (verificationObj.getIsVerified() == true){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already verified.");
        }
        if (LocalDateTime.now().isAfter(verificationObj.getExpiryTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Token expired. Please request a new verification link.");
        }
        verificationObj.setIsVerified(true);
        EmailVerificationType type = verificationObj.getType();
        switch (type){
            case SIGNUP:
                singUpUser();
                break;
            case EMAIL_CHANGE:
                userService.changeEmail(verificationObj.getUser(), verificationObj.getEmail());
                break;
            case PASSWORD_RESET:
                changePassword();
                break;
        }

    }

    private void singUpUser(){

    }



    private void changePassword() {

    }
}
