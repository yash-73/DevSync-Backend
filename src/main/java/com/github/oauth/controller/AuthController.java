package com.github.oauth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> response = Map.of(
                "authenticated", true,
                "user", oauth2User.getAttributes());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/authenticated")
    public ResponseEntity<?> isAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        return ResponseEntity.ok(Map.of("authenticated", true));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear the security context
        SecurityContextHolder.clearContext();

        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear all cookies
        response.setHeader("Set-Cookie", "JSESSIONID=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");

        // Perform Spring Security logout
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}