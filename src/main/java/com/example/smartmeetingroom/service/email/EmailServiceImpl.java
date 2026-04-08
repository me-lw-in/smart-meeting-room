package com.example.smartmeetingroom.service.email;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService{


    private JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String toEmail, String token, String expiryTime) {
        log.info("Sending verification email to: {}", toEmail);
        String subject = "Verify your email";
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;


        String message = "Click the link to verify your email:\n" + verificationLink + "\n\nThis link expires at: " + expiryTime;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setSubject(subject);
        System.out.println(message);
        mail.setText(message);

        try {
            javaMailSender.send(mail);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (RuntimeException e){
            log.error("Failed to send email to: {}", toEmail, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to send email. Please try again later.");
        }
    }
}
