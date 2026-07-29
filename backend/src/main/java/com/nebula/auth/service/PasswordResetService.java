package com.nebula.auth.service;

public interface PasswordResetService {
    void sendOtp(String email);

    String verifyOtp(String email, String otp);

    void resetPassword(String email, String resetToken, String newPassword);
}
