package com.github.oauth.controller;

import com.github.oauth.model.User;
import com.github.oauth.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.Optional;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null) {
            logger.warn("User not authenticated");
            return "redirect:/";
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
                // Handle JWT authentication
                githubId = authentication.getName();
                logger.info("User authenticated via JWT: {}", githubId);
            }

            Optional<User> userOptional = userRepository.findByGithubId(githubId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);
                logger.info("User dashboard accessed for: {}", user.getLogin());
                return "dashboard";
            } else {
                logger.warn("User not found in database for GitHub ID: {}", githubId);
                return "redirect:/";
            }
        } catch (Exception e) {
            logger.error("Error accessing dashboard", e);
            return "redirect:/";
        }
    }
}
