package com.nebula.auth.service;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private Environment environment;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendOtpEmailShouldRenderHtmlOtpBody() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(environment.getProperty("SMTP_USER")).thenReturn("test@example.com");
        when(environment.getProperty("SMTP_PASS")).thenReturn("secret");

        emailService.sendOtpEmail("user@example.com", "4827");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        String content = captor.getValue().getSubject();
        assertThat(content).contains("Reset Password OTP");
    }

    @Test
    void sendOtpEmailShouldNotThrowWhenMailAuthenticationFails() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(environment.getProperty("SMTP_USER")).thenReturn("test@example.com");
        when(environment.getProperty("SMTP_PASS")).thenReturn("secret");
        doThrow(new MailAuthenticationException("Authentication failed")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> emailService.sendOtpEmail("user@example.com", "4827")).doesNotThrowAnyException();
    }

    @Test
    void sendOtpEmailShouldUseSpringMailPropertiesWhenPresent() throws Exception {
        JavaMailSender springMailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(springMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "spring.mail.username", "test@example.com",
                    "spring.mail.password", "secret-password"
            )));
            context.getBeanFactory().registerSingleton("mailSender", springMailSender);
            context.registerBean(EmailService.class);
            context.refresh();

            EmailService mailService = context.getBean(EmailService.class);
            mailService.sendOtpEmail("user@example.com", "4827");

            verify(springMailSender).send(any(MimeMessage.class));
        }
    }

    private String extractText(Object part) throws Exception {
        if (part instanceof Part partValue) {
            Object content = partValue.getContent();
            if (content instanceof String text) {
                return text;
            }
            if (content instanceof Multipart multipart) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < multipart.getCount(); i++) {
                    builder.append(extractText(multipart.getBodyPart(i)));
                }
                return builder.toString();
            }
            return content.toString();
        }
        return part.toString();
    }
}
