package com.github.oauth.controller;

import com.github.oauth.model.AppRole;
import com.github.oauth.model.User;
import com.github.oauth.model.Tech;
import com.github.oauth.payload.ProjectDTO;
import com.github.oauth.payload.UserDTO;
import com.github.oauth.service.UserService;
import com.github.oauth.exception.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> addRoles(Authentication authentication, @RequestBody Map<String, Set<String>> request) {
        Set<String> roleNames = request.get("roles");
        if (roleNames == null || roleNames.isEmpty()) {
            return ResponseEntity.badRequest().body("Roles cannot be empty");
        }

        try {
            // Convert role names to AppRole enum
            Set<AppRole> roles = roleNames.stream()
                    .map(roleName -> {
                        try {
                            return AppRole.valueOf(roleName.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Invalid role name: " + roleName +
                                    ". Valid roles are: " + String.join(", ", AppRole.values().toString()));
                        }
                    })
                    .collect(Collectors.toSet());

            // Add each role to the user
            for (AppRole role : roles) {
                userService.addRoleToUser(authentication, role);
            }

            logger.info("Roles {} added successfully", roles);
            return ResponseEntity.ok("Roles added successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add roles: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding roles", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/tech")
    public ResponseEntity<?> addTech(Authentication authentication, @RequestBody Map<String, Set<String>> request) {
        Set<String> techStack = request.get("techStack");
        if (techStack == null || techStack.isEmpty()) {
            return ResponseEntity.badRequest().body("Tech stack cannot be empty");
        }
        logger.info("Tech stack: {}", techStack);
        try {
            User user = userService.getCurrentUser(authentication);
            Set<Tech> updatedTechStack = userService.addTech(techStack, user);
            logger.info("Tech stack updated successfully for user: {}", user.getLogin());
            return ResponseEntity.ok(updatedTechStack);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add tech stack: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding tech stack", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/tech/{technology}")
    public ResponseEntity<?> removeTech(Authentication authentication, @PathVariable String technology) {
        if (technology == null || technology.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Technology name cannot be empty");
        }

        try {
            User user = userService.getCurrentUser(authentication);
            Set<Tech> updatedTechStack = userService.removeTech(technology, user);
            logger.info("Technology {} removed successfully for user: {}", technology, user.getLogin());
            return ResponseEntity.ok(updatedTechStack);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to remove technology: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error removing technology", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getMyCreatedProjects(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            List<ProjectDTO> projects = userService.getMyCreatedProjects(user);
            logger.info("Retrieved {} projects for user: {}", projects.size(), user.getLogin());
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve projects: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving projects", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/tech")
    public ResponseEntity<?> getTechStack(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            Set<Tech> techStack = userService.getTechStack(user);
            logger.info("Retrieved tech stack for user: {}", user.getLogin());
            return ResponseEntity.ok(techStack);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve tech stack: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving tech stack", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/joined-projects")
    public ResponseEntity<?> getProjects(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            List<ProjectDTO> projects = userService.getProjects(user);
            logger.info("Retrieved {} projects for user: {}", projects.size(), user.getLogin());
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve projects: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving projects", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/dto")
    public ResponseEntity<?> getUserDTO(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            UserDTO userDTO = userService.getUserDTO(user);
            logger.info("Retrieved user DTO for: {}", user.getLogin());
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve user DTO: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving user DTO", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(Authentication authentication, @PathVariable Long userId) {
        try {
            // Verify authentication
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                throw new IllegalArgumentException("User not authenticated");
            }

            UserDTO userDTO = userService.getUserDTOById(userId);
            logger.info("User DTO retrieved for userId: {}", userId);
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve user DTO: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (ResourceNotFound e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving user DTO", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{userId}/projects")
    public ResponseEntity<?> getCreatedProjectsByUserId(Authentication authentication, @PathVariable Long userId) {
        try {
            // Verify authentication
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                throw new IllegalArgumentException("User not authenticated");
            }

            List<ProjectDTO> projects = userService.getCreatedProjectsByUserId(userId);
            logger.info("Retrieved {} created projects for userId: {}", projects.size(), userId);
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve created projects: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (ResourceNotFound e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving created projects", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{userId}/joined-projects")
    public ResponseEntity<?> getJoinedProjectsByUserId(Authentication authentication, @PathVariable Long userId) {
        try {
            // Verify authentication
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                throw new IllegalArgumentException("User not authenticated");
            }

            List<ProjectDTO> projects = userService.getJoinedProjectsByUserId(userId);
            logger.info("Retrieved {} joined projects for userId: {}", projects.size(), userId);
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve joined projects: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (ResourceNotFound e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving joined projects", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}