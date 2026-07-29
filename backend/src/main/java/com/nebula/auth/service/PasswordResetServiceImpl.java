package com.nebula.auth.service;

import com.nebula.auth.exception.InvalidOtpException;
import com.nebula.auth.model.PasswordResetOtp;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.PasswordResetOtpRepository;
import com.nebula.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int OTP_LENGTH = 4;
    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetOtpRepository passwordResetOtpRepository,
                                    PasswordEncoder passwordEncoder,
                                    EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("No account found with this email"));

        passwordResetOtpRepository.deleteByEmail(email);

        String otp = generateOtp();
        PasswordResetOtp resetOtp = new PasswordResetOtp();
        resetOtp.setUserId(user.getId());
        resetOtp.setEmail(user.getEmail());
        resetOtp.setOtpHash(passwordEncoder.encode(otp));
        resetOtp.setCreatedAt(LocalDateTime.now());
        resetOtp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES));
        resetOtp.setVerified(false);
        resetOtp.setUsed(false);
        resetOtp.setAttempts(0);
        resetOtp.setBlocked(false);

        passwordResetOtpRepository.save(resetOtp);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Override
    public String verifyOtp(String email, String otp) {
        PasswordResetOtp resetOtp = passwordResetOtpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new InvalidOtpException("No OTP was found for this email"));

        if (resetOtp.isBlocked()) {
            throw new InvalidOtpException("Too many failed attempts. Please request a new OTP");
        }

        if (resetOtp.isUsed()) {
            throw new InvalidOtpException("OTP already used");
        }

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP expired");
        }

        if (!passwordEncoder.matches(otp, resetOtp.getOtpHash())) {
            resetOtp.setAttempts(resetOtp.getAttempts() + 1);
            resetOtp.setLastAttemptAt(LocalDateTime.now());
            if (resetOtp.getAttempts() >= MAX_ATTEMPTS) {
                resetOtp.setBlocked(true);
            }
            passwordResetOtpRepository.save(resetOtp);
            throw new InvalidOtpException("Invalid OTP");
        }

        String resetToken = generateResetToken();
        resetOtp.setVerified(true);
        resetOtp.setResetTokenHash(passwordEncoder.encode(resetToken));
        resetOtp.setAttempts(0);
        passwordResetOtpRepository.save(resetOtp);
        return resetToken;
    }

    @Override
    public void resetPassword(String email, String resetToken, String newPassword) {
        PasswordResetOtp resetOtp = passwordResetOtpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new InvalidOtpException("No OTP was found for this email"));

        if (!resetOtp.isVerified()) {
            throw new InvalidOtpException("OTP has not been verified yet");
        }

        if (resetOtp.isUsed()) {
            throw new InvalidOtpException("OTP already used");
        }

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP expired");
        }

        if (resetOtp.getResetTokenHash() == null || !passwordEncoder.matches(resetToken, resetOtp.getResetTokenHash())) {
            throw new InvalidOtpException("Invalid or expired reset session");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("No account found with this email"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetOtp.setUsed(true);
        passwordResetOtpRepository.save(resetOtp);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 1000 + random.nextInt(9000);
        return String.format("%04d", otp);
    }

    private String generateResetToken() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
