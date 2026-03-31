package com.example.smartmeetingroom.service.verification;

import com.example.smartmeetingroom.dto.user.EmailDTO;

public interface EmailVerificationService {

    public String verifyToken(String token);

    public void resendEmailForVerification(EmailDTO dto);

    public void changeEmail(EmailDTO dto);

    public void singUpUser(EmailDTO dto);

    public void requestPasswordReset(EmailDTO dto);
}