package com.example.smartmeetingroom.service.email;

import java.util.List;

public interface EmailService {

    public void sendEmail(String to, List<String> cc, List<String> bcc, String subject, String body);
}
