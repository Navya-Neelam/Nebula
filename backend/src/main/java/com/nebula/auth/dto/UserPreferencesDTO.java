package com.nebula.auth.dto;

public class UserPreferencesDTO {

    private String theme;
    private String language;
    private String timeZone;
    private Boolean emailNotifications;
    private Boolean marketingEmails;

    public UserPreferencesDTO() {
    }

    public UserPreferencesDTO(String theme, String language, String timeZone, Boolean emailNotifications, Boolean marketingEmails) {
        this.theme = theme;
        this.language = language;
        this.timeZone = timeZone;
        this.emailNotifications = emailNotifications;
        this.marketingEmails = marketingEmails;
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

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getMarketingEmails() {
        return marketingEmails;
    }

    public void setMarketingEmails(Boolean marketingEmails) {
        this.marketingEmails = marketingEmails;
    }
}
