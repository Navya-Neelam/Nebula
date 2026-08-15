package com.nebula.auth.security;

import com.nebula.auth.model.LoginHistory;
import com.nebula.auth.model.RefreshToken;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.LoginHistoryRepository;
import com.nebula.auth.repository.RefreshTokenRepository;
import com.nebula.auth.repository.UserRepository;
import com.nebula.auth.util.UserAgentUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                LoginHistoryRepository loginHistoryRepository,
                                RefreshTokenRepository refreshTokenRepository,
                                JwtService jwtService) {
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthToken.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        String email = extractEmail(clientRegistrationId, attributes);
        String name = extractName(clientRegistrationId, attributes, email);
        String profileImage = extractProfileImage(clientRegistrationId, attributes);
        String provider = clientRegistrationId.toUpperCase();

        if (email == null || email.isBlank()) {
            response.sendRedirect("http://localhost:4200/login?error=OAuth2%20email%20not%20provided");
            return;
        }

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = UserAgentUtils.getClientIp(request);
        String device = UserAgentUtils.extractDevice(userAgent);
        String browser = UserAgentUtils.extractBrowser(userAgent);
        String os = UserAgentUtils.extractOs(userAgent);
        String location = UserAgentUtils.extractLocation(ipAddress);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setRole("STUDENT");
            newUser.setVerified(true);
            newUser.setActive(true);
            newUser.setPassword(UUID.randomUUID().toString());
            newUser.setCreatedAt(LocalDateTime.now());
            return newUser;
        });

        user.setOauthProvider(provider);
        user.setLoginMethod(provider);
        user.setLastLogin(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setLastLoginDevice(device);
        user.setLastLoginBrowser(browser);
        if (profileImage != null && (user.getProfileImageUrl() == null || user.getProfileImageUrl().isBlank())) {
            user.setProfileImageUrl(profileImage);
        }
        userRepository.save(user);

        LoginHistory history = new LoginHistory(user.getId(), user.getEmail(), ipAddress, device, browser, os, location, provider, "SUCCESS", userAgent);
        loginHistoryRepository.save(history);

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenStr,
                user.getEmail(),
                LocalDateTime.now().plusDays(7),
                device,
                browser,
                os,
                location,
                ipAddress,
                false
        );
        refreshTokenRepository.save(refreshToken);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/login")
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshTokenStr)
                .queryParam("role", user.getRole())
                .queryParam("id", user.getId())
                .queryParam("fullName", user.getFullName())
                .queryParam("email", user.getEmail())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        if ("github".equalsIgnoreCase(provider)) {
            Object emailObj = attributes.get("email");
            if (emailObj != null && !emailObj.toString().isBlank()) {
                return emailObj.toString();
            }
            Object loginObj = attributes.get("login");
            if (loginObj != null) {
                return loginObj.toString() + "@github.com";
            }
        }
        Object email = attributes.get("email");
        return email != null ? email.toString() : null;
    }

    private String extractName(String provider, Map<String, Object> attributes, String fallbackEmail) {
        Object name = attributes.get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        Object login = attributes.get("login");
        if (login != null && !login.toString().isBlank()) {
            return login.toString();
        }
        return fallbackEmail != null ? fallbackEmail.split("@")[0] : "OAuth User";
    }

    private String extractProfileImage(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            Object picture = attributes.get("picture");
            return picture != null ? picture.toString() : null;
        }
        if ("github".equalsIgnoreCase(provider)) {
            Object avatar = attributes.get("avatar_url");
            return avatar != null ? avatar.toString() : null;
        }
        return null;
    }
}
