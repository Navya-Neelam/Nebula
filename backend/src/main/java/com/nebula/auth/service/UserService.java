package com.nebula.auth.service;

import com.nebula.auth.dto.ActiveSessionDTO;
import com.nebula.auth.dto.AuthResponse;
import com.nebula.auth.dto.LoginRequest;
import com.nebula.auth.dto.RegisterRequest;
import com.nebula.auth.dto.SendOtpRequest;
import com.nebula.auth.dto.UserResponse;
import com.nebula.auth.dto.VerifyLoginOtpRequest;
import com.nebula.auth.exception.EmailAlreadyExistsException;
import com.nebula.auth.exception.InvalidOtpException;
import com.nebula.auth.model.LoginHistory;
import com.nebula.auth.model.OnboardingStatus;
import com.nebula.auth.model.OtpVerification;
import com.nebula.auth.model.PasswordResetToken;
import com.nebula.auth.model.RefreshToken;
import com.nebula.auth.model.User;
import com.nebula.auth.model.UserPreferences;
import com.nebula.auth.model.VerificationToken;
import com.nebula.auth.repository.LoginHistoryRepository;
import com.nebula.auth.repository.OtpVerificationRepository;
import com.nebula.auth.repository.PasswordResetTokenRepository;
import com.nebula.auth.repository.RefreshTokenRepository;
import com.nebula.auth.repository.UserRepository;
import com.nebula.auth.repository.VerificationTokenRepository;
import com.nebula.auth.security.JwtService;
import com.nebula.auth.util.UserAgentUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProfileCompletionService profileCompletionService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole("STUDENT");
        user.setVerified(true);
        user.setActive(true);

        // Step 1: Names
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        } else if (user.getFirstName() != null) {
            user.setFullName(((user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "")).trim());
        }

        if (user.getFirstName() == null && user.getFullName() != null) {
            String[] parts = user.getFullName().trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
        }

        // Step 3: Contact & Location
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getTimeZone() != null) {
            user.setTimezone(request.getTimeZone());
        }

        // Step 4: Preferences
        UserPreferences preferences;
        if (request.getUserPreferences() != null) {
            preferences = new UserPreferences(
                    request.getUserPreferences().getTheme() != null ? request.getUserPreferences().getTheme() : "SYSTEM",
                    request.getUserPreferences().getLanguage() != null ? request.getUserPreferences().getLanguage() : "en",
                    request.getUserPreferences().getTimeZone() != null ? request.getUserPreferences().getTimeZone() : (user.getTimezone() != null ? user.getTimezone() : "UTC"),
                    request.getUserPreferences().getEmailNotifications() != null ? request.getUserPreferences().getEmailNotifications() : true,
                    request.getUserPreferences().getMarketingEmails() != null ? request.getUserPreferences().getMarketingEmails() : false
            );
        } else {
            preferences = new UserPreferences("SYSTEM", "en", user.getTimezone() != null ? user.getTimezone() : "UTC", true, false);
        }
        user.setUserPreferences(preferences);

        // Track Onboarding Status
        OnboardingStatus status = new OnboardingStatus(true, false, request.getUserPreferences() != null, false);
        user.setOnboardingStatus(status);

        // Calculate Profile Completion
        profileCompletionService.calculateCompletionForUser(user);

        User savedUser = userRepository.save(user);

        // Update IDs on sub-documents
        preferences.setUserId(savedUser.getId());
        status.setUserId(savedUser.getId());
        userRepository.save(savedUser);

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
        return login(request, null);
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "";
        String ipAddress = UserAgentUtils.getClientIp(httpRequest);
        String browser = UserAgentUtils.extractBrowser(userAgent);
        String device = UserAgentUtils.extractDevice(userAgent);
        String os = UserAgentUtils.extractOs(userAgent);
        String location = UserAgentUtils.extractLocation(ipAddress);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    recordFailedLogin(request.getEmail(), ipAddress, device, browser, os, location, "PASSWORD", userAgent);
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordFailedLogin(request.getEmail(), ipAddress, device, browser, os, location, "PASSWORD", userAgent);
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Email not verified. Please verify your email first.");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Your account is deactivated. Please contact the administrator.");
        }

        user.setRememberMeEnabled(request.isRememberMe());
        user.setLastLogin(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setLastLoginDevice(device);
        user.setLastLoginBrowser(browser);
        user.setLoginMethod("PASSWORD");
        userRepository.save(user);

        LoginHistory history = new LoginHistory(user.getId(), user.getEmail(), ipAddress, device, browser, os, location, "PASSWORD", "SUCCESS", userAgent);
        loginHistoryRepository.save(history);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        String refreshTokenStr = UUID.randomUUID().toString();
        int expiryDays = request.isRememberMe() ? 30 : 7;
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenStr,
                user.getEmail(),
                LocalDateTime.now().plusDays(expiryDays),
                device,
                browser,
                os,
                location,
                ipAddress,
                request.isRememberMe()
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

    private void recordFailedLogin(String email, String ip, String device, String browser, String os, String location, String method, String ua) {
        LoginHistory history = new LoginHistory(null, email, ip, device, browser, os, location, method, "FAILED", ua);
        loginHistoryRepository.save(history);
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
        user.setUpdatedAt(LocalDateTime.now());
        profileCompletionService.calculateCompletionForUser(user);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (oldRefreshToken.isRevoked()) {
            refreshTokenRepository.deleteByEmail(oldRefreshToken.getEmail());
            throw new BadCredentialsException("Refresh token has been revoked. Potential reuse attempt detected. Please login again.");
        }

        if (oldRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(oldRefreshToken);
            throw new BadCredentialsException("Refresh token has expired. Please login again.");
        }

        User user = userRepository.findByEmail(oldRefreshToken.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new BadCredentialsException("User is deactivated");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Email not verified");
        }

        // --- REFRESH TOKEN ROTATION ---
        // Delete old token
        refreshTokenRepository.delete(oldRefreshToken);

        // Issue new rotated refresh token
        String newRefreshTokenStr = UUID.randomUUID().toString();
        boolean isRememberMe = oldRefreshToken.isRememberMe();
        int expiryDays = isRememberMe ? 30 : 7;

        RefreshToken newRefreshToken = new RefreshToken(
                newRefreshTokenStr,
                user.getEmail(),
                LocalDateTime.now().plusDays(expiryDays),
                oldRefreshToken.getDevice(),
                oldRefreshToken.getBrowser(),
                oldRefreshToken.getIpAddress(),
                isRememberMe
        );
        refreshTokenRepository.save(newRefreshToken);

        // Issue new access token
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(
                newAccessToken,
                newRefreshTokenStr,
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
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "reset");
        String resetTokenStr = jwtService.generateToken(claims, email);

        passwordResetTokenRepository.deleteByEmail(email);
        PasswordResetToken resetToken = new PasswordResetToken(
                resetTokenStr,
                email,
                LocalDateTime.now().plusMinutes(15)
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

    public String sendLoginOtp(SendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("No account registered with this email address"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Your account is deactivated. Please contact the administrator.");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Email not verified. Please verify your email first.");
        }

        // Rate limit / Resend protection (must wait 60s)
        otpVerificationRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(user.getEmail(), "LOGIN")
                .ifPresent(existing -> {
                    if (existing.getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                        throw new RuntimeException("Please wait 60 seconds before requesting another OTP.");
                    }
                });

        // Generate 6-digit OTP
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));
        String hashedOtp = passwordEncoder.encode(otp);

        // Deactivate previous login OTPs for this email
        otpVerificationRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(user.getEmail(), "LOGIN")
                .ifPresent(old -> {
                    old.setUsed(true);
                    otpVerificationRepository.save(old);
                });

        OtpVerification otpVerification = new OtpVerification(
                user.getEmail(),
                hashedOtp,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5),
                "LOGIN",
                false
        );
        otpVerificationRepository.save(otpVerification);

        emailService.sendLoginOtpEmail(user.getEmail(), otp);

        return "OTP sent successfully to " + user.getEmail();
    }

    public AuthResponse verifyLoginOtp(VerifyLoginOtpRequest request) {
        return verifyLoginOtp(request, null);
    }

    public AuthResponse verifyLoginOtp(VerifyLoginOtpRequest request, HttpServletRequest httpRequest) {
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "";
        String ipAddress = UserAgentUtils.getClientIp(httpRequest);
        String browser = UserAgentUtils.extractBrowser(userAgent);
        String device = UserAgentUtils.extractDevice(userAgent);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Email not verified");
        }

        OtpVerification otpVerification = otpVerificationRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(request.getEmail(), "LOGIN")
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP. Please request a new code."));

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpVerification.setUsed(true);
            otpVerificationRepository.save(otpVerification);
            throw new InvalidOtpException("OTP has expired. Please request a new code.");
        }

        if (otpVerification.getAttempts() >= 5) {
            otpVerification.setUsed(true);
            otpVerificationRepository.save(otpVerification);
            throw new InvalidOtpException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        otpVerification.setAttempts(otpVerification.getAttempts() + 1);

        if (!passwordEncoder.matches(request.getOtp(), otpVerification.getOtpHash())) {
            otpVerificationRepository.save(otpVerification);
            int remaining = 5 - otpVerification.getAttempts();
            throw new InvalidOtpException("Invalid OTP code. " + remaining + " attempts remaining.");
        }

        // Success: Mark verified & used
        otpVerification.setVerified(true);
        otpVerification.setUsed(true);
        otpVerificationRepository.save(otpVerification);

        // Update User login metadata
        user.setRememberMeEnabled(request.isRememberMe());
        user.setLastLogin(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setLastLoginDevice(device);
        user.setLastLoginBrowser(browser);
        user.setLoginMethod("OTP");
        userRepository.save(user);

        LoginHistory history = new LoginHistory(user.getId(), user.getEmail(), ipAddress, device, browser, "OTP", "SUCCESS", userAgent);
        loginHistoryRepository.save(history);

        // Generate Access & Refresh tokens
        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        refreshTokenRepository.deleteByEmail(user.getEmail());
        String refreshTokenStr = UUID.randomUUID().toString();
        int expiryDays = request.isRememberMe() ? 30 : 7;
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenStr,
                user.getEmail(),
                LocalDateTime.now().plusDays(expiryDays),
                device,
                browser,
                ipAddress,
                request.isRememberMe()
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

    public List<ActiveSessionDTO> getActiveSessions(String email, String currentRefreshToken) {
        return refreshTokenRepository.findAll().stream()
                .filter(rt -> email.equalsIgnoreCase(rt.getEmail()) && !rt.isRevoked() && rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(rt -> new ActiveSessionDTO(
                        rt.getId(),
                        rt.getDevice() != null ? rt.getDevice() : "Desktop Device",
                        rt.getBrowser() != null ? rt.getBrowser() : "Standard Browser",
                        rt.getOs() != null ? rt.getOs() : "Unknown OS",
                        rt.getLocation() != null ? rt.getLocation() : "Local Network",
                        rt.getIpAddress() != null ? rt.getIpAddress() : "127.0.0.1",
                        rt.getCreatedAt() != null ? rt.getCreatedAt() : LocalDateTime.now(),
                        rt.getExpiresAt(),
                        currentRefreshToken != null && currentRefreshToken.equals(rt.getToken()),
                        rt.isRememberMe()
                ))
                .collect(Collectors.toList());
    }

    public void revokeSession(String email, String sessionId) {
        refreshTokenRepository.findById(sessionId)
                .ifPresent(token -> {
                    if (email.equalsIgnoreCase(token.getEmail())) {
                        refreshTokenRepository.delete(token);
                    }
                });
    }

    public void revokeAllSessions(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    public void revokeAllOtherSessions(String email, String currentRefreshToken) {
        List<RefreshToken> userTokens = refreshTokenRepository.findByEmail(email);
        for (RefreshToken rt : userTokens) {
            if (!rt.getToken().equals(currentRefreshToken)) {
                refreshTokenRepository.delete(rt);
            }
        }
    }

    public List<LoginHistory> getLoginHistory(String email) {
        return loginHistoryRepository.findTop20ByEmailOrderByLoginTimeDesc(email);
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
        user.setUpdatedAt(LocalDateTime.now());
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
                user.getPhone() != null ? user.getPhone() : user.getPhoneNumber(),
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
        user.setPhone(updateDto.getPhoneNumber());
        user.setBio(updateDto.getBio());
        if (updateDto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(updateDto.getProfileImageUrl());
        }

        user.setUpdatedAt(LocalDateTime.now());
        profileCompletionService.calculateCompletionForUser(user);

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
                savedUser.getPhone() != null ? savedUser.getPhone() : savedUser.getPhoneNumber(),
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
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
