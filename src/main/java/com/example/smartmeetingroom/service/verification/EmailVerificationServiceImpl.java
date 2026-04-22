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
    public void singUpUser(EmailDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email already exists.");
        }

        if (emailVerificationRepository.existsByEmailAndTypeAndIsUsed(email, EmailVerificationType.SIGNUP, false)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Verification already sent. Please check your email.");
        }

        var token = generateVerificationToken();
        // set expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedTime = expirationTime.format(formatter);

        var verificationObj = new EmailVerification();
        verificationObj.setEmail(email);
        verificationObj.setToken(token);
        verificationObj.setType(EmailVerificationType.SIGNUP);
        verificationObj.setExpiryTime(expirationTime);
        verificationObj.setUser(null);
        emailVerificationRepository.save(verificationObj);
        sendVerificationToken(email, token, formattedTime);
    }

    @Transactional
    public void resendEmailForVerification(EmailDTO dto) {
        String email = dto.getEmail().toLowerCase().trim();
        if (dto.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type is required");
        }
        var type = dto.getType();
        var verificationObj = emailVerificationRepository.findByEmailAndTypeAndIsUsed(email, type,false);

        if (verificationObj.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Please send email for verification");
        }

        var currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null && type == EmailVerificationType.EMAIL_CHANGE){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please login first to change email.");
        }
        String newToken = generateVerificationToken();
        // set expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedTime = expirationTime.format(formatter);

        verificationObj.get().setToken(newToken);
        verificationObj.get().setExpiryTime(expirationTime);
        verificationObj.get().setVerifiedAt(null);
        verificationObj.get().setVerifiedValidUntil(null);

        sendVerificationToken(email, newToken, formattedTime);


    }

    // change email
    @Transactional
    public void changeEmail(EmailDTO dto){
        String email = dto.getEmail().toLowerCase().trim();
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please authenticate");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }
        if (emailVerificationRepository.existsByEmailAndTypeAndIsUsed(email, EmailVerificationType.EMAIL_CHANGE, false)) {
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
        sendVerificationToken(email, token, formattedTime);
    }

    private String generateVerificationToken(){
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (emailVerificationRepository.existsByToken(token));

        return token;
    }


    @Transactional
    public String verifyToken(String token) {
        String frontEndPort = "3000";
        String frontEndDomain = "localhost";
        var verificationObj = emailVerificationRepository.findByToken(token).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token!")
        );

        if (Boolean.TRUE.equals(verificationObj.getIsUsed())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Process already completed.");
        }

        if (verificationObj.getVerifiedAt() != null){
            if (LocalDateTime.now().isAfter(verificationObj.getVerifiedValidUntil())) {
                // here i should delete that token right? //go to resend
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Verification session expired. Please restart.");
            }

            // return url
            if (verificationObj.getType() == EmailVerificationType.SIGNUP){
                return "http://" + frontEndDomain + ":" + frontEndPort + "/auth/complete-signup?token=" + token;
            }else if (verificationObj.getType() == EmailVerificationType.PASSWORD_RESET){
                return "http://" + frontEndDomain + ":" + frontEndPort + "/user/change-password?token=" + token;
            }
        }

        if (LocalDateTime.now().isAfter(verificationObj.getExpiryTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Token expired. Please request a new verification link.");
        }



        var verifiedAt = LocalDateTime.now();
        var verificationValidTill = verifiedAt.plusHours(1);
        verificationObj.setVerifiedAt(verifiedAt);
        verificationObj.setVerifiedValidUntil(verificationValidTill);
        EmailVerificationType type = verificationObj.getType();
        switch (type){
            case SIGNUP:
                System.out.println("return url");
                return "http://" + frontEndDomain + ":" + frontEndPort + "/auth/complete-signup?token=" + token;

            case EMAIL_CHANGE:
                verificationObj.setIsUsed(true);
                userService.changeEmail(verificationObj.getUser(), verificationObj.getEmail());
                break;
            case PASSWORD_RESET:
                return "http://" + frontEndDomain + ":" + frontEndPort + "/user/change-password?token=" + token;

        }
        return null;
    }


    @Transactional
    public void requestPasswordReset(EmailDTO dto) {
        var email = dto.getEmail().trim().toLowerCase();
        var user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Please enter valid email.")
        );

        if (emailVerificationRepository.existsByEmailAndTypeAndIsUsed(email, EmailVerificationType.PASSWORD_RESET, false)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Verification link already sent.");
        }

        var token = generateVerificationToken();
        // set expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedTime = expirationTime.format(formatter);

        var verificationObj = new EmailVerification();
        verificationObj.setEmail(email);
        verificationObj.setToken(token);
        verificationObj.setType(EmailVerificationType.PASSWORD_RESET);
        verificationObj.setExpiryTime(expirationTime);
        verificationObj.setUser(user);
        emailVerificationRepository.save(verificationObj);
        sendVerificationToken(email, token, formattedTime);

    }

    private void sendVerificationToken(String to, String token, String expiryTime){
        String subject = "Verify your email";
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;
        String body = "Click the link to verify your email:\n" + verificationLink + "\n\nThis link expires at: " + expiryTime;
        emailService.sendEmail(to, null, null, subject, body);
    }
}
