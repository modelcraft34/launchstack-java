package com.launchstack.email;

public interface EmailService {

    void sendEmailVerificationEmail(String to, String token, String verificationLink);

    void sendPasswordResetEmail(String to, String token, String resetLink);
}
