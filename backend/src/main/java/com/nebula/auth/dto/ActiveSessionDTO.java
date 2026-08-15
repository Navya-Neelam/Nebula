package com.nebula.auth.dto;

import java.time.LocalDateTime;

public class ActiveSessionDTO {

    private String id;
    private String device;
    private String browser;
    private String os;
    private String location;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean currentSession;
    private boolean rememberMe;

    public ActiveSessionDTO() {
    }

    public ActiveSessionDTO(String id, String device, String browser, String os, String location, String ipAddress, LocalDateTime createdAt, LocalDateTime expiresAt, boolean currentSession, boolean rememberMe) {
        this.id = id;
        this.device = device;
        this.browser = browser;
        this.os = os;
        this.location = location;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.currentSession = currentSession;
        this.rememberMe = rememberMe;
    }

    public ActiveSessionDTO(String id, String device, String browser, String ipAddress, LocalDateTime createdAt, LocalDateTime expiresAt, boolean currentSession, boolean rememberMe) {
        this(id, device, browser, "Unknown OS", "Local Network", ipAddress, createdAt, expiresAt, currentSession, rememberMe);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(boolean currentSession) {
        this.currentSession = currentSession;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
