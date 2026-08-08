package com.nebula.auth.service;

import com.nebula.auth.dto.UserPreferencesDTO;
import com.nebula.auth.model.OnboardingStatus;
import com.nebula.auth.model.User;
import com.nebula.auth.model.UserPreferences;
import com.nebula.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserPreferencesService {

    private final UserRepository userRepository;
    private final ProfileCompletionService profileCompletionService;

    public UserPreferencesService(UserRepository userRepository, ProfileCompletionService profileCompletionService) {
        this.userRepository = userRepository;
        this.profileCompletionService = profileCompletionService;
    }

    public UserPreferencesDTO getPreferences(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreferences pref = user.getUserPreferences();
        if (pref == null) {
            pref = new UserPreferences("SYSTEM", "en", user.getTimezone() != null ? user.getTimezone() : "UTC", true, false);
            user.setUserPreferences(pref);
            userRepository.save(user);
        }

        return new UserPreferencesDTO(
                pref.getTheme(),
                pref.getLanguage(),
                pref.getTimeZone(),
                pref.isEmailNotifications(),
                pref.isMarketingEmails()
        );
    }

    public UserPreferencesDTO updatePreferences(String email, UserPreferencesDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreferences pref = user.getUserPreferences();
        if (pref == null) {
            pref = new UserPreferences();
            pref.setUserId(user.getId());
        }

        if (dto.getTheme() != null) {
            pref.setTheme(dto.getTheme());
        }
        if (dto.getLanguage() != null) {
            pref.getLanguage();
            pref.setLanguage(dto.getLanguage());
        }
        if (dto.getTimeZone() != null) {
            pref.setTimeZone(dto.getTimeZone());
            user.setTimezone(dto.getTimeZone());
        }
        if (dto.getEmailNotifications() != null) {
            pref.setEmailNotifications(dto.getEmailNotifications());
        }
        if (dto.getMarketingEmails() != null) {
            pref.setMarketingEmails(dto.getMarketingEmails());
        }

        user.setUserPreferences(pref);
        
        // Update Onboarding status preferencesSaved to true
        OnboardingStatus status = user.getOnboardingStatus();
        if (status == null) {
            status = new OnboardingStatus(true, false, true, false);
            user.setOnboardingStatus(status);
        } else {
            status.setPreferencesSaved(true);
        }

        user.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate completion
        profileCompletionService.calculateCompletionForUser(user);

        userRepository.save(user);

        return new UserPreferencesDTO(
                pref.getTheme(),
                pref.getLanguage(),
                pref.getTimeZone(),
                pref.isEmailNotifications(),
                pref.isMarketingEmails()
        );
    }
}
