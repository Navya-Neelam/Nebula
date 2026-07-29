package com.nebula.auth.service;

import com.nebula.auth.dto.AuthResponse;
import com.nebula.auth.dto.LoginRequest;
import com.nebula.auth.dto.RegisterRequest;
import com.nebula.auth.dto.UserResponse;
import com.nebula.auth.exception.EmailAlreadyExistsException;
import com.nebula.auth.model.User;
import com.nebula.auth.model.VerificationToken;
import com.nebula.auth.model.PasswordResetToken;
import com.nebula.auth.model.RefreshToken;
import com.nebula.auth.repository.UserRepository;
import com.nebula.auth.repository.VerificationTokenRepository;
import com.nebula.auth.repository.PasswordResetTokenRepository;
import com.nebula.auth.repository.RefreshTokenRepository;
import com.nebula.auth.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                LocalDateTime.now()
        );
        user.setRole("STUDENT"); // Feature 1: default role STUDENT
        user.setVerified(false); // Feature 2: account inactive until verified
        user.setActive(true);

        String fullName = request.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            if (parts.length > 1) {
                user.setLastName(parts[1]);
            } else {
                user.setLastName("");
            }
        }

        User savedUser = userRepository.save(user);

        // Generate verification token
        String tokenStr = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                tokenStr,
                savedUser.getEmail(),
                LocalDateTime.now().plusHours(24)
        );
        verificationTokenRepository.save(verificationToken);

        // Send email verification link
        emailService.sendVerificationEmail(savedUser.getEmail(), tokenStr);

        return new AuthResponse(
                null,
                null,
                savedUser.getRole(),
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Email not verified. Please verify your email first.");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Your account is deactivated. Please contact the administrator.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        // Generate refresh token (Feature 4)
        refreshTokenRepository.deleteByEmail(user.getEmail()); // delete old ones
        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenStr,
                user.getEmail(),
                LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                token,
                refreshTokenStr,
                user.getRole(),
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }

    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new RuntimeException("Verification token has expired. Please register again.");
        }

        User user = userRepository.findByEmail(verificationToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh token has expired. Please login again.");
        }

        User user = userRepository.findByEmail(refreshToken.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new BadCredentialsException("User is deactivated");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Email not verified");
        }

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(
                newAccessToken,
                refreshTokenStr,
                user.getRole(),
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }

    public void logout(String refreshTokenStr) {
        refreshTokenRepository.deleteByToken(refreshTokenStr);
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Do not reveal whether user exists
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "reset");
        String resetTokenStr = jwtService.generateToken(claims, email);

        passwordResetTokenRepository.deleteByEmail(email); // delete old ones
        PasswordResetToken resetToken = new PasswordResetToken(
                resetTokenStr,
                email,
                LocalDateTime.now().plusMinutes(15) // expires in 15 mins
        );
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordReset(email, resetTokenStr);
        return resetTokenStr;
    }

    public String verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        PasswordResetToken resetToken = passwordResetTokenRepository.findByEmail(email)
                .orElseGet(() -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("purpose", "reset");
                    String tokenStr = jwtService.generateToken(claims, email);
                    PasswordResetToken newTk = new PasswordResetToken(tokenStr, email, LocalDateTime.now().plusMinutes(15));
                    return passwordResetTokenRepository.save(newTk);
                });

        return resetToken.getToken();
    }

    public void resetPassword(String token, String newPassword) {
        String email = jwtService.extractEmail(token);
        if (!jwtService.isTokenValid(token, email)) {
            throw new RuntimeException("Invalid or expired token");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or already used token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token is invalid or expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getRole(),
                user.isVerified(),
                user.isActive(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getBio(),
                user.getProfileImageUrl()
        );
    }

    public UserResponse updateProfile(String email, UserResponse updateDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setFullName((updateDto.getFirstName() + " " + updateDto.getLastName()).trim());
        user.setPhoneNumber(updateDto.getPhoneNumber());
        user.setBio(updateDto.getBio());
        if (updateDto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(updateDto.getProfileImageUrl());
        }

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt(),
                savedUser.getRole(),
                savedUser.isVerified(),
                savedUser.isActive(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhoneNumber(),
                savedUser.getBio(),
                savedUser.getProfileImageUrl()
        );
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
