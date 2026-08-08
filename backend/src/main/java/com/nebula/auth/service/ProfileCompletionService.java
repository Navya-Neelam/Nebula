package com.nebula.auth.service;

import com.nebula.auth.dto.ProfileCompletionDTO;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileCompletionService {

    private final UserRepository userRepository;

    public ProfileCompletionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileCompletionDTO calculateCompletion(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return calculateCompletionForUser(user);
    }

    public ProfileCompletionDTO calculateCompletionForUser(User user) {
        int percentage = 0;
        List<String> completedItems = new ArrayList<>();
        List<String> pendingItems = new ArrayList<>();

        // 1. Basic Information (30%)
        if (user.getFirstName() != null && !user.getFirstName().isBlank()
                && user.getLastName() != null && !user.getLastName().isBlank()
                && user.getEmail() != null && !user.getEmail().isBlank()) {
            percentage += 30;
            completedItems.add("Basic Information");
        } else {
            pendingItems.add("Basic Information");
        }

        // 2. Phone Added (20%)
        String phone = user.getPhone() != null ? user.getPhone() : user.getPhoneNumber();
        if (phone != null && !phone.isBlank()) {
            percentage += 20;
            completedItems.add("Phone Added");
        } else {
            pendingItems.add("Phone Added");
        }

        // 3. Preferences Saved (20%)
        if (user.getUserPreferences() != null
                || (user.getOnboardingStatus() != null && user.getOnboardingStatus().isPreferencesSaved())) {
            percentage += 20;
            completedItems.add("Preferences Saved");
        } else {
            pendingItems.add("Preferences Saved");
        }

        // 4. Profile Picture (15%)
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            percentage += 15;
            completedItems.add("Profile Picture");
        } else {
            pendingItems.add("Profile Picture");
        }

        // 5. Email Verified (15%)
        if (user.isVerified()) {
            percentage += 15;
            completedItems.add("Email Verified");
        } else {
            pendingItems.add("Email Verified");
        }

        ProfileCompletionDTO dto = new ProfileCompletionDTO(percentage, completedItems, pendingItems);
        
        // Update profileCompletion field on user if percentage changed or null
        user.setProfileCompletion(dto);
        if (user.getOnboardingStatus() != null) {
            user.getOnboardingStatus().setProfileCompleted(percentage >= 100);
        }
        
        return dto;
    }
}
