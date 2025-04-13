package com.github.oauth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
   
    private static final String FRONTEND_URL = "http://localhost:5173";

    public OAuth2AuthenticationSuccessHandler() {
        setDefaultTargetUrl(FRONTEND_URL + "/dashboard");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            logger.info("OAuth2 authentication successful");
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // String githubId = attributes.get("id").toString();
            String login = (String) attributes.get("login");
            logger.info("Authenticated GitHub user: {}", login);

            // Redirect to frontend dashboard
            response.sendRedirect(FRONTEND_URL);
        } catch (Exception e) {
            logger.error("Error during OAuth2 authentication success handling", e);
            response.sendRedirect(FRONTEND_URL + "/?error=auth_failed");
        }
    }
}