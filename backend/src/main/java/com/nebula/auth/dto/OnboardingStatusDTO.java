package com.nebula.auth.dto;

public class OnboardingStatusDTO {

    private Boolean registrationCompleted;
    private Boolean welcomeScreenViewed;
    private Boolean preferencesSaved;
    private Boolean profileCompleted;

    public OnboardingStatusDTO() {
    }

    public OnboardingStatusDTO(Boolean registrationCompleted, Boolean welcomeScreenViewed, Boolean preferencesSaved, Boolean profileCompleted) {
        this.registrationCompleted = registrationCompleted;
        this.welcomeScreenViewed = welcomeScreenViewed;
        this.preferencesSaved = preferencesSaved;
        this.profileCompleted = profileCompleted;
    }

    public Boolean getRegistrationCompleted() {
        return registrationCompleted;
    }

    public void setRegistrationCompleted(Boolean registrationCompleted) {
        this.registrationCompleted = registrationCompleted;
    }

    public Boolean getWelcomeScreenViewed() {
        return welcomeScreenViewed;
    }

    public void setWelcomeScreenViewed(Boolean welcomeScreenViewed) {
        this.welcomeScreenViewed = welcomeScreenViewed;
    }

    public Boolean getPreferencesSaved() {
        return preferencesSaved;
    }

    public void setPreferencesSaved(Boolean preferencesSaved) {
        this.preferencesSaved = preferencesSaved;
    }

    public Boolean getProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
}
