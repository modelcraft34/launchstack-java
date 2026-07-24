package com.launchstack.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendEmailVerificationEmail(String to, String token, String verificationLink) {
        String body = "Please verify your LaunchStack account.\n"
                + "Verification link: " + verificationLink + "\n"
                + "Verification token: " + token + "\n"
                + "If you did not create this account, you can ignore this email.";
        logEmail(to, "LaunchStack Email Verification", body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token, String resetLink) {
        String body = "You requested a LaunchStack password reset.\n"
                + "Reset link: " + resetLink + "\n"
                + "Reset token: " + token + "\n"
                + "If you did not request this, you can ignore this email.";
        logEmail(to, "LaunchStack Password Reset", body);
    }

    private void logEmail(String to, String subject, String body) {
        LOGGER.info("Local email delivery -> to: {}, subject: {}, body: {}", to, subject, body);
    }
}
