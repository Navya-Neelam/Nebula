package com.nebula.auth.service;

public interface PasswordResetService {
    void sendOtp(String email);

    void verifyOtp(String email, String otp);

    void resetPassword(String email, String otp, String newPassword);
}
