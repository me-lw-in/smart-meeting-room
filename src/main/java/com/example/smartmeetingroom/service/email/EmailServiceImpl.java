package com.example.smartmeetingroom.service.email;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService{


    private JavaMailSender javaMailSender;

    @Async
    @Override
    public void sendEmail(String to, List<String> cc, List<String> bcc, String subject, String body) {
        log.info("Sending verification email to: {}", to);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);

        if (cc != null && !cc.isEmpty()) {
            mail.setCc(cc.toArray(new String[0]));
        }

        if (bcc != null && !bcc.isEmpty()) {
            mail.setBcc(bcc.toArray(new String[0]));
        }

        mail.setSubject(subject);
        mail.setText(body);

        try {
            javaMailSender.send(mail);
            log.info("Email sent successfully to: {}", to);
        } catch (RuntimeException e){
            log.error("Failed to send email to: {}", to, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to send email. Please try again later.");
        }
    }
}
