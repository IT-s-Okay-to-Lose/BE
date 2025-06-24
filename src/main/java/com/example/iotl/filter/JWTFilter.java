package com.example.iotl.filter;

import com.example.iotl.dto.CustomOAuth2User;
import com.example.iotl.dto.UserDto;
import com.example.iotl.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    //Header에서 AccessCookie꺼내기
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String uri = request.getRequestURI();
//
//        // Spring Security에서 permitAll로 지정한 경로는 인증 스킵
//        if (isPermitAllPath(uri)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String header = request.getHeader("Authorization");
//
//        if (header == null || !header.startsWith("Bearer ")) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
//            return;
//        }
//
//        String accessToken = header.substring(7);
//
//        try {
//            jwtUtil.isExpired(accessToken);
//        } catch (ExpiredJwtException e) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\": \"Access token expired\"}");
//            return;
//        }
//
//        if (!"access".equals(jwtUtil.getCategory(accessToken))) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\": \"Invalid token category\"}");
//            return;
//        }
//
//        String username = jwtUtil.getUsername(accessToken);
//        String role = jwtUtil.getRole(accessToken);
//
//
////        // ✅ 여기서 로그 찍기
////        log.info("🔐 JWT Filter activated");
////        log.info("🪪 Authorization Header: {}", header);
////        log.info("👤 Username from JWT: {}", username);
////        log.info("🎭 Role from JWT: {}", role);
////
//
//        UserDto userDto = new UserDto();
//        userDto.setUsername(username);
//        userDto.setRole(role);
//
//        CustomOAuth2User customUserDetails = new CustomOAuth2User(userDto);
//        Authentication authToken = new UsernamePasswordAuthenticationToken(
//                customUserDetails, null, customUserDetails.getAuthorities()
//        );
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(request, response);
//    }


@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

    String uri = request.getRequestURI();

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


    // 🔽 이 함수 추가
    private boolean isPermitAllPath(String uri) {
        return uri.equals("/")
                || uri.equals("/login")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login/oauth2/")
                || uri.equals("/reissue")
                || uri.startsWith("/ws/")
                || uri.startsWith("/api/stocks/meta")
                || uri.startsWith("/api/stocks/dynamic")
                || uri.startsWith("/api/stocks/chart/")
                || uri.equals("/api/exchange")
                || uri.startsWith("/api/stocks/") // candle 포함
                || uri.startsWith("/api/market-index/") // candle 포함
                || uri.startsWith("/v3/api-docs/")
                || uri.startsWith("/swagger-ui/")
                || uri.equals("/swagger-ui.html")
                || uri.equals("/api/v1/news/top3");

    }
}