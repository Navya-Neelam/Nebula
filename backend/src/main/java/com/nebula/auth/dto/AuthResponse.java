package com.nebula.auth.dto;

public class AuthResponse {
    private String token;
    private String id;
    private String fullName;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, String id, String fullName, String email) {
        this.token = token;
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
