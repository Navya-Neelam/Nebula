package com.nebula.auth.controller;

import com.nebula.auth.dto.OnboardingStatusDTO;
import com.nebula.auth.service.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/status")
    public ResponseEntity<OnboardingStatusDTO> getOnboardingStatus() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        OnboardingStatusDTO status = onboardingService.getOnboardingStatus(email);
        return ResponseEntity.ok(status);
    }

    @PutMapping("/status")
    public ResponseEntity<OnboardingStatusDTO> updateOnboardingStatus(@RequestBody OnboardingStatusDTO statusDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        OnboardingStatusDTO updated = onboardingService.updateOnboardingStatus(email, statusDTO);
        return ResponseEntity.ok(updated);
    }
}
