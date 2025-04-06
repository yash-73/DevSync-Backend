package com.github.oauth.service;

import com.github.oauth.model.User;
import com.github.oauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateEmail(Authentication authentication, String newEmail) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalArgumentException("User not authenticated");
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Object githubIdObj = oAuth2User.getAttribute("id");
        if (githubIdObj == null) {
            throw new IllegalArgumentException("GitHub ID not found in authentication");
        }
        String githubId = githubIdObj.toString();

        userRepository.updateEmailByGithubId(githubId, newEmail);
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalArgumentException("User not authenticated");
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Object githubIdObj = oAuth2User.getAttribute("id");
        if (githubIdObj == null) {
            throw new IllegalArgumentException("GitHub ID not found in authentication");
        }
        String githubId = githubIdObj.toString();

        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
} 