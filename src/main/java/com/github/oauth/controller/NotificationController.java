package com.github.oauth.controller;

import com.github.oauth.model.ProjectJoinRequest;
import com.github.oauth.model.User;
import com.github.oauth.service.NotificationService;
import com.github.oauth.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/notification")

public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final UserService userService;
    private final NotificationService notificationService;

    public NotificationController(UserService userService, NotificationService notificationService){
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @PostMapping("/join-request")
    public ResponseEntity<?> addJoinRequest(Authentication authentication, @RequestBody ProjectJoinRequest request) {
        try {
            User user = userService.getCurrentUser(authentication);
            String response = notificationService.addRequest(request, user);
            logger.info("Join request added successfully by user: {}", user.getLogin());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add join request: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding join request", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/update-request")
    public ResponseEntity<?> acceptRequest(Authentication authentication, @RequestBody ProjectJoinRequest request) {
        try {
            User creator = userService.getCurrentUser(authentication);
            String response = notificationService.updateRequest(request, creator);
            logger.info("Join request updated successfully by user: {}", creator.getLogin());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update join request: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating join request", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/seen-request/{userId}/{projectId}")
    public ResponseEntity<?> seenRequest(Authentication authentication, @PathVariable Long userId, @PathVariable Long projectId) {
        try {
            User user = userService.getCurrentUser(authentication);
            String response = notificationService.deleteRequest(userId, projectId, user);
            logger.info("Join request marked as seen by user: {}", user.getLogin());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to mark join request as seen: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error marking join request as seen", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/own-request/{projectId}")
    public ResponseEntity<?> deleteOwnRequest(Authentication authentication, @PathVariable Long projectId) {
        try {
            User user = userService.getCurrentUser(authentication);
            String response = notificationService.deleteOwnRequest(projectId, user);
            logger.info("User {} deleted their own pending join request for project {}", user.getLogin(), projectId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete own join request: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting own join request", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
