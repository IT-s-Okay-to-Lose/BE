package com.example.iotl.handler;

import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.service.security.CustomOAuth2UserService;
import com.example.iotl.service.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ObjectMapper objectMapper;

    public CustomSuccessHandler(TokenService tokenService, CustomOAuth2UserService customOAuth2UserService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        log.info("<CustomSuccessHandler - onAuthenticationSuccess> requestURL = {}", request.getRequestURL().toString());

        Map<String, String> tokens = tokenService.createTokens(username, role);
        String access = tokens.get("access");
        String refresh = tokens.get("refresh");

        // ✅ Access Token → JS 접근 가능 쿠키로
        ResponseCookie accessCookie = tokenService.createAccessCookie(access);
        response.addHeader("Set-Cookie", accessCookie.toString());

        // ✅ Refresh Token → HttpOnly 쿠키로
        ResponseCookie refreshCookie = tokenService.createRefreshCookie(refresh);
        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("Redirecting to front - main Page After Successful Login");
        // ✅ 리다이렉트로 프론트 진입
        response.sendRedirect("https://iotl-fe.vercel.app/");
//        response.sendRedirect("https://localhost:5137/");
    }
}
