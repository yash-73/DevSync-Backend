package com.github.oauth.controller;

import com.github.oauth.model.User;
import com.github.oauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null) {
            logger.warn("User not authenticated");
            return ResponseEntity.status(401).body("User not authenticated");
        }

        try {
            Object principal = authentication.getPrincipal();
            String githubId;

            if (principal instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) principal;
                Map<String, Object> attributes = oAuth2User.getAttributes();
                githubId = attributes.get("id").toString();
                logger.info("User authenticated via OAuth2: {}", attributes.get("login"));
            } else {
                githubId = authentication.getName();
                logger.info("User authenticated via: {}", githubId);
            }

            Optional<User> userOptional = userRepository.findByGithubId(githubId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                logger.info("User profile accessed for: {}", user.getLogin());
                return ResponseEntity.ok(user);
            } else {
                logger.warn("User not found in database for GitHub ID: {}", githubId);
                return ResponseEntity.status(404).body("User not found");
            }
        } catch (Exception e) {
            logger.error("Error accessing user profile", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(Authentication authentication, @RequestBody Map<String, String> request) {
        if (authentication == null) {
            logger.warn("User not authenticated");
            return ResponseEntity.status(401).body("User not authenticated");
        }

        String newEmail = request.get("email");
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email cannot be empty");
        }

        try {
            Object principal = authentication.getPrincipal();
            String githubId;

            if (principal instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) principal;
                Map<String, Object> attributes = oAuth2User.getAttributes();
                githubId = attributes.get("id").toString();
                logger.info("User authenticated via OAuth2: {}", attributes.get("login"));
            } else {
                githubId = authentication.getName();
                logger.info("User authenticated via: {}", githubId);
            }

            Optional<User> userOptional = userRepository.findByGithubId(githubId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setEmail(newEmail);
                userRepository.save(user);
                logger.info("Email updated for user: {}", user.getLogin());
                return ResponseEntity.ok("Email updated successfully");
            } else {
                logger.warn("User not found in database for GitHub ID: {}", githubId);
                return ResponseEntity.status(404).body("User not found");
            }
        } catch (Exception e) {
            logger.error("Error updating email", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
} 