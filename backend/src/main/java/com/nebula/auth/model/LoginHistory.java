package com.nebula.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "login_history")
public class LoginHistory {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String email;

    private String ipAddress;
    private String device;
    private String browser;
    private String os;
    private String location;
    private String loginMethod; // "PASSWORD", "OTP", "GOOGLE", "GITHUB"
    private LocalDateTime loginTime;
    private String status; // "SUCCESS", "FAILED"
    private String userAgent;

    public LoginHistory() {
        this.loginTime = LocalDateTime.now();
    }

    public LoginHistory(String userId, String email, String ipAddress, String device, String browser, String os, String location, String loginMethod, String status, String userAgent) {
        this.userId = userId;
        this.email = email;
        this.ipAddress = ipAddress;
        this.device = device;
        this.browser = browser;
        this.os = os;
        this.location = location;
        this.loginMethod = loginMethod;
        this.loginTime = LocalDateTime.now();
        this.status = status;
        this.userAgent = userAgent;
    }

    public LoginHistory(String userId, String email, String ipAddress, String device, String browser, String loginMethod, String status, String userAgent) {
        this(userId, email, ipAddress, device, browser, "Unknown OS", "Local Network", loginMethod, status, userAgent);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(String loginMethod) {
        this.loginMethod = loginMethod;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
