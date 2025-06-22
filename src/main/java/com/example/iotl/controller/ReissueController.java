package com.example.iotl.controller;

import com.example.iotl.jwt.JWTUtil;
import com.example.iotl.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    public ReissueController(JWTUtil jwtUtil, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }

        if (refresh == null) {
            return ResponseEntity.badRequest().body("refresh token null");
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.badRequest().body("refresh token expired");
        }

        if (!"refresh".equals(jwtUtil.getCategory(refresh))) {
            return ResponseEntity.badRequest().body("invalid refresh token");
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        Map<String, String> tokens = tokenService.rotateRefreshToken(username, role, refresh, 600000L, 604800000L);
        String newAccess = tokens.get("access");
        String newRefresh = tokens.get("refresh");

        // Access → 헤더
        response.setHeader("Authorization", "Bearer " + newAccess);

        // Refresh → ResponseCookie
        ResponseCookie refreshCookie = tokenService.createResponseCookie("refresh", newRefresh);
        response.setHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok().build();
    }
}
