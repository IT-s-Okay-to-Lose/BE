package com.example.iotl.filter;

import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.dto.UserDto;
import com.example.iotl.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final List<String> permitAllPaths;

    public JWTFilter(JWTUtil jwtUtil,  List<String> permitAllPaths) {
        this.jwtUtil = jwtUtil;
        this.permitAllPaths = permitAllPaths;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.info("Requested URI: {}", uri);
        log.info("Permit all paths: {}", permitAllPaths);

        // ✅ WebSocket 요청은 무조건 허용
        if (uri.startsWith("/ws/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPermitAllPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ access 쿠키에서 JWT 토큰 꺼내기
        String accessToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null || accessToken.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing access token in cookie\"}");
            return;
        }

        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Access token expired\"}");
            return;
        }

        if (!"access".equals(jwtUtil.getCategory(accessToken))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token category\"}");
            return;
        }

        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setRole(role);

        CustomOAuth2User customUserDetails = new CustomOAuth2User(userDto);
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }


    // 설정한 api 경로 허용
    private boolean isPermitAllPath(String uri) {
        return permitAllPaths.stream()
                .anyMatch(path -> {
                    if (path.endsWith("/")) {
                        return uri.startsWith(path); // 경로 접두사 매칭
                    } else {
                        return uri.equals(path); // 정확히 일치
                    }
                });
    }
}