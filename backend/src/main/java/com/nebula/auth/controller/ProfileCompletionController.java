package com.nebula.auth.controller;

import com.nebula.auth.dto.ProfileCompletionDTO;
import com.nebula.auth.service.ProfileCompletionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileCompletionController {

    private final ProfileCompletionService profileCompletionService;

    public ProfileCompletionController(ProfileCompletionService profileCompletionService) {
        this.profileCompletionService = profileCompletionService;
    }

    @GetMapping("/completion")
    public ResponseEntity<ProfileCompletionDTO> getProfileCompletion() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ProfileCompletionDTO completion = profileCompletionService.calculateCompletion(email);
        return ResponseEntity.ok(completion);
    }
}
