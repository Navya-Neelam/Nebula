package com.nebula.auth.service;

import com.nebula.auth.dto.OnboardingStatusDTO;
import com.nebula.auth.model.OnboardingStatus;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OnboardingService {

    private final UserRepository userRepository;
    private final ProfileCompletionService profileCompletionService;

    public OnboardingService(UserRepository userRepository, ProfileCompletionService profileCompletionService) {
        this.userRepository = userRepository;
        this.profileCompletionService = profileCompletionService;
    }

    public OnboardingStatusDTO getOnboardingStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OnboardingStatus status = user.getOnboardingStatus();
        if (status == null) {
            status = new OnboardingStatus(true, false, user.getUserPreferences() != null, false);
            user.setOnboardingStatus(status);
            userRepository.save(user);
        }

        return new OnboardingStatusDTO(
                status.isRegistrationCompleted(),
                status.isWelcomeScreenViewed(),
                status.isPreferencesSaved(),
                status.isProfileCompleted()
        );
    }

    public OnboardingStatusDTO updateOnboardingStatus(String email, OnboardingStatusDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OnboardingStatus status = user.getOnboardingStatus();
        if (status == null) {
            status = new OnboardingStatus();
            status.setUserId(user.getId());
        }

        if (dto.getRegistrationCompleted() != null) {
            status.setRegistrationCompleted(dto.getRegistrationCompleted());
        }
        if (dto.getWelcomeScreenViewed() != null) {
            status.setWelcomeScreenViewed(dto.getWelcomeScreenViewed());
        }
        if (dto.getPreferencesSaved() != null) {
            status.setPreferencesSaved(dto.getPreferencesSaved());
        }
        if (dto.getProfileCompleted() != null) {
            status.setProfileCompleted(dto.getProfileCompleted());
        }

        user.setOnboardingStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate profile completion
        profileCompletionService.calculateCompletionForUser(user);

        userRepository.save(user);

        return new OnboardingStatusDTO(
                status.isRegistrationCompleted(),
                status.isWelcomeScreenViewed(),
                status.isPreferencesSaved(),
                status.isProfileCompleted()
        );
    }
}
