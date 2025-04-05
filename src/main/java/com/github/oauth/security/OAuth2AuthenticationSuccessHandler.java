package com.github.oauth.security;



import com.github.oauth.repository.UserRepository;
import com.github.oauth.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;


    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository,JwtTokenProvider jwtTokenProvider,
                                              CookieUtil cookieUtil){
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieUtil = cookieUtil;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String githubId = attributes.get("id").toString();

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(githubId);

        // Add the token to a cookie
        cookieUtil.addJwtCookie(response, token);

        // Redirect to the home page or dashboard
        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
    }
}