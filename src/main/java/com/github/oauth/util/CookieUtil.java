package com.github.oauth.util;



import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Data
public class CookieUtil {

    @Value("${app.cookie.jwt-cookie-name}")
    private String jwtCookieName;

    @Value("${app.cookie.domain}")
    private String domain;

    @Value("${app.cookie.path}")
    private String path;

    @Value("${app.cookie.max-age}")
    private int maxAge;

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.http-only}")
    private boolean httpOnly;

    public void addJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(jwtCookieName, token);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        response.addCookie(cookie);
    }

    public void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtCookieName, null);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        response.addCookie(cookie);
    }
}