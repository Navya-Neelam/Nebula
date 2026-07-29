package com.nebula.auth.dto;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private String role;
    private String id;
    private String fullName;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, String refreshToken, String role, String id, String fullName, String email) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.role = role;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
