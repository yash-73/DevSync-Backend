package com.github.oauth.controller;

import com.github.oauth.model.User;
import com.github.oauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            logger.warn("User not authenticated or not an OAuth2 user");
            return "redirect:/";
        }

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String githubId = attributes.get("id").toString();

            Optional<User> userOptional = userRepository.findByGithubId(githubId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);
                logger.info("User dashboard accessed for: {}", user.getLogin());
            } else {
                logger.warn("User not found in database for GitHub ID: {}", githubId);
                return "redirect:/";
            }

            return "dashboard";
        } catch (Exception e) {
            logger.error("Error accessing dashboard", e);
            return "redirect:/";
        }
    }
}
