package com.nebula.auth.service;

import com.nebula.auth.model.PasswordResetOtp;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.PasswordResetOtpRepository;
import com.nebula.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Test
    void sendOtpShouldPersistOtpAndSendEmailWhenUserExists() {
        User user = new User();
        user.setId("user-1");
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        doNothing().when(passwordResetOtpRepository).deleteByEmail("user@example.com");
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "hashed:" + invocation.getArgument(0));

        passwordResetService.sendOtp("user@example.com");

        ArgumentCaptor<PasswordResetOtp> otpCaptor = ArgumentCaptor.forClass(PasswordResetOtp.class);
        verify(passwordResetOtpRepository).deleteByEmail("user@example.com");
        verify(passwordResetOtpRepository).save(otpCaptor.capture());
        verify(emailService).sendOtpEmail(eq("user@example.com"), anyString());

        PasswordResetOtp savedOtp = otpCaptor.getValue();
        assertThat(savedOtp.getEmail()).isEqualTo("user@example.com");
        assertThat(savedOtp.getOtpHash()).startsWith("hashed:");
        assertThat(savedOtp.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(savedOtp.isVerified()).isFalse();
        assertThat(savedOtp.isUsed()).isFalse();
    }
}
