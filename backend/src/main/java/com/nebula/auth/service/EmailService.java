package com.nebula.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final Environment environment;

    @Value("${mail-defaults.from:${MAIL_FROM:no-reply@nebula.local}}")
    private String fromAddress;

    @Value("${FRONTEND_URL:http://localhost:4200}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender, Environment environment) {
        this.mailSender = mailSender;
        this.environment = environment;
    }

    public void sendPasswordReset(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String html = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; background: #f8fafc; border-radius: 12px;'>"
                + "<h2 style='color: #7c3aed;'>Password Reset</h2>"
                + "<p>We received a request to reset your password.</p>"
                + "<p><a href='" + resetLink + "' style='display:inline-block;padding:12px 20px;background:#7c3aed;color:#fff;text-decoration:none;border-radius:8px;'>Reset Password</a></p>"
                + "<p>If you did not request this, you can safely ignore this email.</p>"
                + "</div>";

        sendHtmlEmail(toEmail, "Nebula Auth - Password Reset", html);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = frontendUrl + "/verify?token=" + token;
        String html = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; background: #f8fafc; border-radius: 12px;'>"
                + "<h2 style='color: #14b8a6;'>Verify Your Email</h2>"
                + "<p>Welcome to Nebula Academy! Please verify your email address to activate your account.</p>"
                + "<p><a href='" + verifyLink + "' style='display:inline-block;padding:12px 20px;background:#14b8a6;color:#fff;text-decoration:none;border-radius:8px;'>Verify Email</a></p>"
                + "<p>If you did not create an account, you can safely ignore this email.</p>"
                + "</div>";

        sendHtmlEmail(toEmail, "Nebula Academy - Verify Your Email", html);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        if (!shouldSendEmail()) {
            log.info("--- [DEVELOPMENT/TEST MODE] OTP generated for {}: {} ---", toEmail, otp);
        }

        String html = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; background: #f8fafc; border-radius: 12px;'>"
                + "<h2 style='color: #7c3aed;'>Reset Password OTP</h2>"
                + "<p>Hello,</p>"
                + "<p>Your OTP for resetting your password is:</p>"
                + "<div style='font-size: 32px; font-weight: 700; letter-spacing: 6px; color: #111827; margin: 20px 0; padding: 16px 20px; background: #ffffff; border-radius: 8px; display: inline-block;'>" + otp + "</div>"
                + "<p>This OTP is valid for 5 minutes.</p>"
                + "<p>If you did not request this password reset, please ignore this email.</p>"
                + "<p>Thank you.</p>"
                + "</div>";

        sendHtmlEmail(toEmail, "Reset Password OTP", html);
    }

    public void sendLoginOtpEmail(String toEmail, String otp) {
        if (!shouldSendEmail()) {
            log.info("--- [DEVELOPMENT/TEST MODE] LOGIN OTP generated for {}: {} ---", toEmail, otp);
        }

        String html = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; background: #f8fafc; border-radius: 12px;'>"
                + "<h2 style='color: #a855f7;'>Nebula Auth - Your Login OTP</h2>"
                + "<p>Hello,</p>"
                + "<p>Your 6-digit OTP to log in to your Nebula account is:</p>"
                + "<div style='font-size: 32px; font-weight: 700; letter-spacing: 6px; color: #a855f7; margin: 20px 0; padding: 16px 20px; background: #ffffff; border: 1px solid #e5e7eb; border-radius: 8px; display: inline-block;'>" + otp + "</div>"
                + "<p>This OTP is valid for 5 minutes. Do not share this code with anyone.</p>"
                + "<p>If you did not request this login code, please ignore this email.</p>"
                + "<p>Thank you,<br/>Nebula Security Team</p>"
                + "</div>";

        sendHtmlEmail(toEmail, "Nebula Auth - Login OTP Code", html);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        if (!shouldSendEmail()) {
            log.warn("Email sending skipped for {} because SMTP credentials are not configured. OTP was generated but no email was sent.", toEmail);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            String sender = (fromAddress == null || fromAddress.isBlank() || fromAddress.equals("no-reply@nebula.local"))
                    ? resolveProperty("SMTP_USER", "MAIL_USERNAME", "spring.mail.username")
                    : fromAddress;
            helper.setFrom(sender);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException | RuntimeException ex) {
            log.warn("Failed to send email to {}. The password reset flow will continue without email delivery. Cause: {}", toEmail, ex.getMessage());
        }
    }

    private boolean shouldSendEmail() {
        String username = resolveProperty("SMTP_USER", "MAIL_USERNAME", "spring.mail.username");
        String password = resolveProperty("SMTP_PASS", "MAIL_PASSWORD", "spring.mail.password");
        return !username.isBlank() && !password.isBlank()
                && !username.contains("your-email")
                && !password.contains("your-app-password");
    }

    private String resolveProperty(String... keys) {
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
