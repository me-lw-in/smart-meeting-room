package com.example.smartmeetingroom.service.email;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EmailService {

    public void sendEmail(String to, List<String> cc, List<String> bcc, String subject, String body);

    public void sendMeetingEmails(
            Set<Long> participantIds,
            Long loggedInUserId,
            LocalDateTime startTime,
            String meetingRoom
    );
}
