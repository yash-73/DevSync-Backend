package com.github.oauth.controller;

import com.github.oauth.model.AppRole;

import com.github.oauth.model.User;


import com.github.oauth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            logger.info("User profile accessed for: {}", user.getLogin());
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            logger.warn("User not authenticated or not found: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error accessing user profile", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(Authentication authentication, @RequestBody Map<String, String> request) {
        String newEmail = request.get("email");
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email cannot be empty");
        }

        try {
            userService.updateEmail(authentication, newEmail);
            logger.info("Email updated successfully");
            return ResponseEntity.ok("Email updated successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update email: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating email", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/role")
    public ResponseEntity<?> addRole(Authentication authentication, @RequestBody Map<String, String> request) {
        String roleName = request.get("role");
        if (roleName == null || roleName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role name cannot be empty");
        }

        try {
            AppRole appRole;
            try {
                appRole = AppRole.valueOf(roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid role name. Valid roles are: " + 
                    String.join(", ", AppRole.values().toString()));
            }

            userService.addRoleToUser(authentication, appRole);
            logger.info("Role {} added successfully", appRole);
            return ResponseEntity.ok("Role added successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add role: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding role", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
} 