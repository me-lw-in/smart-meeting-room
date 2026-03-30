package com.example.smartmeetingroom.service.email;

import java.time.LocalDateTime;

public interface EmailService {

    public void sendEmail(String toEmail, String token, String expiryTime);
}
