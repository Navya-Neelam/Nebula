package com.nebula.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "onboarding_status")
public class OnboardingStatus {

    @Id
    private String id;
    private String userId;
    private boolean registrationCompleted = false;
    private boolean welcomeScreenViewed = false;
    private boolean preferencesSaved = false;
    private boolean profileCompleted = false;

    public OnboardingStatus() {
    }

    public OnboardingStatus(boolean registrationCompleted, boolean welcomeScreenViewed, boolean preferencesSaved, boolean profileCompleted) {
        this.registrationCompleted = registrationCompleted;
        this.welcomeScreenViewed = welcomeScreenViewed;
        this.preferencesSaved = preferencesSaved;
        this.profileCompleted = profileCompleted;
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

    public boolean isRegistrationCompleted() {
        return registrationCompleted;
    }

    public void setRegistrationCompleted(boolean registrationCompleted) {
        this.registrationCompleted = registrationCompleted;
    }

    public boolean isWelcomeScreenViewed() {
        return welcomeScreenViewed;
    }

    public void setWelcomeScreenViewed(boolean welcomeScreenViewed) {
        this.welcomeScreenViewed = welcomeScreenViewed;
    }

    public boolean isPreferencesSaved() {
        return preferencesSaved;
    }

    public void setPreferencesSaved(boolean preferencesSaved) {
        this.preferencesSaved = preferencesSaved;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
}
