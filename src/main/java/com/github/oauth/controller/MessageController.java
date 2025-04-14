package com.github.oauth.controller;

import com.github.oauth.model.Message;
import com.github.oauth.model.User;
import com.github.oauth.service.MessageService;
import com.github.oauth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping("/addMessage")
    public ResponseEntity<?> addMessage(@RequestBody Message message, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized attempt to add message");
                return ResponseEntity.status(401).body("Authentication required");
            }

            User user = userService.getCurrentUser(authentication);
            String result = messageService.addMessage(message, user);
            logger.info("Message added successfully by user: {}", user.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error adding message", e);
            return ResponseEntity.status(500).body("Failed to add message: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteMessage")
    public ResponseEntity<?> deleteMessage(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized attempt to delete message");
                return ResponseEntity.status(401).body("Authentication required");
            }

            String messageId = request.get("messageId");
            if (messageId == null) {
                throw new IllegalArgumentException("Message ID is required");
            }

            User user = userService.getCurrentUser(authentication);
            String result = messageService.deleteMessage(messageId, user);
            logger.info("Message deleted successfully by user: {}", user.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error deleting message", e);
            return ResponseEntity.status(500).body("Failed to delete message: " + e.getMessage());
        }
    }
} 