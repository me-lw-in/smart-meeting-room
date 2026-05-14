package com.example.smartmeetingroom.service.email;

import com.example.smartmeetingroom.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService{


    private JavaMailSender javaMailSender;
    private final UserRepository userRepository;

    @Async
    @Override
    public void sendEmail(String to, List<String> cc, List<String> bcc, String subject, String body) {
        log.info("Sending  email to: {}", to);

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

    @Async
    @Override
    public void sendMeetingEmails(Set<Long> participantIds, Long loggedInUserId, LocalDateTime startTime, String meetingRoom) {

        for (Long userId : participantIds) {
            var user = userRepository.findById(userId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );
            String body;
            if (userId.equals(loggedInUserId)) {
                body = "Your meeting has been scheduled at "
                        + startTime +
                        " in " +
                        meetingRoom + ".";
            } else {
                body = "You have been invited to a meeting at "
                        + startTime +
                        " in " +
                        meetingRoom + ".";
            }
            sendEmail(
                    user.getEmail(),
                    null,
                    null,
                    "Meeting Scheduled",
                    body
            );
        }
    }
}
