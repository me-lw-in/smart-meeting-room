package com.example.smartmeetingroom.service.verification;

import com.example.smartmeetingroom.dto.user.EmailDTO;

public interface EmailVerificationService {

    public void verifyToken(String token);

    public void resendEmailForVerification(EmailDTO dto);

    public void createEmailForVerification(EmailDTO dto);
}
