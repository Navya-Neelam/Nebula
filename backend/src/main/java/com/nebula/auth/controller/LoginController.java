package com.nebula.auth.controller;

import com.nebula.auth.dto.ActiveSessionDTO;
import com.nebula.auth.model.LoginHistory;
import com.nebula.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<LoginHistory>> getLoginHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<LoginHistory> history = userService.getLoginHistory(email);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/current-session")
    public ResponseEntity<List<ActiveSessionDTO>> getCurrentSession(HttpServletRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentRefreshToken = request.getHeader("X-Refresh-Token");
        List<ActiveSessionDTO> sessions = userService.getActiveSessions(email, currentRefreshToken);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/session/{id}")
    public ResponseEntity<Map<String, String>> revokeSession(@PathVariable("id") String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.revokeSession(email, id);
        return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
    }

    @DeleteMapping("/logout-all")
    public ResponseEntity<Map<String, String>> logoutAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.revokeAllSessions(email);
        return ResponseEntity.ok(Map.of("message", "All active sessions revoked successfully"));
    }
}
