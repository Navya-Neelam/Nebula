package com.nebula.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_preferences")
public class UserPreferences {

    @Id
    private String id;
    private String userId;
    private String theme = "SYSTEM"; // DARK, LIGHT, SYSTEM
    private String language = "en";
    private String timeZone = "UTC";
    private boolean emailNotifications = true;
    private boolean marketingEmails = false;

    public UserPreferences() {
    }

    public UserPreferences(String theme, String language, String timeZone, boolean emailNotifications, boolean marketingEmails) {
        this.theme = theme;
        this.language = language;
        this.timeZone = timeZone;
        this.emailNotifications = emailNotifications;
        this.marketingEmails = marketingEmails;
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isMarketingEmails() {
        return marketingEmails;
    }

    public void setMarketingEmails(boolean marketingEmails) {
        this.marketingEmails = marketingEmails;
    }
}
