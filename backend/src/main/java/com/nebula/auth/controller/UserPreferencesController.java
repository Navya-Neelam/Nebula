package com.nebula.auth.controller;

import com.nebula.auth.dto.UserPreferencesDTO;
import com.nebula.auth.service.UserPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    public UserPreferencesController(UserPreferencesService userPreferencesService) {
        this.userPreferencesService = userPreferencesService;
    }

    @GetMapping
    public ResponseEntity<UserPreferencesDTO> getPreferences() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserPreferencesDTO preferences = userPreferencesService.getPreferences(email);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping
    public ResponseEntity<UserPreferencesDTO> updatePreferences(@RequestBody UserPreferencesDTO preferencesDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserPreferencesDTO updated = userPreferencesService.updatePreferences(email, preferencesDTO);
        return ResponseEntity.ok(updated);
    }
}
