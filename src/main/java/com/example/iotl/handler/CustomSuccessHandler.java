package com.example.iotl.handler;

import com.example.iotl.dto.CustomOAuth2User;
import com.example.iotl.dto.UserInfoDto;
import com.example.iotl.service.CustomOAuth2UserService;
import com.example.iotl.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
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

//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//
//        //OAuth2User
//        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
//        String username = customUserDetails.getUsername();
//
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//        String role = iterator.next().getAuthority();
//
//        // ✅ TokenService로 access, refresh 생성 및 저장 -> access : 15분, refresh : 7일
//        Map<String, String> tokens = tokenService.createTokens(username, role);
//        String access = tokens.get("access");
//        String refresh = tokens.get("refresh");
//
//        log.info("[CustomLoginSuccessHandler] onAuthenticationSuccess - access={} authority={}", access,  role);
//
////        // ✅ 응답에 설정
////        response.setHeader("access", access);
////        response.addCookie(tokenService.createCookie("refresh", refresh));
////        response.setStatus(HttpStatus.OK.value());
////
////        // SPA 적용 안 할 경우 그대로 redirect 유지 -> 근데 우린 프론트팀이 있지롱 ㅎ
////        response.sendRedirect("http://localhost:8080/");
//
//        /*
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpStatus.OK.value());
//
//        // refresh는 여전히 HttpOnly 쿠키로 내려야 함
//        response.addCookie(createCookie("refresh", refresh));
//
//        // access는 JSON으로 내려줌
//        String responseBody = "{ \"access\": \"" + access + "\" }";
//        response.getWriter().write(responseBody);
//         */
//
//        // ✅ 응답 본문을 JSON으로 내려주는 방식으로 변경
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpStatus.OK.value());
//
//        // refresh는 여전히 HttpOnly 쿠키로 내려야 함
//        response.setHeader("Authorization", "Bearer " + access);
//        response.addCookie(tokenService.createCookie("refresh", refresh));
//
//        //user의 이름과 가입일
//        UserInfoDto userInfoDto = customOAuth2UserService.getNameAndCreatedAt(username);
////        response.getWriter().write(new ObjectMapper().writeValueAsString(userInfoDto));
//        response.getWriter().write(objectMapper.writeValueAsString(userInfoDto));
//
////        // access는 JSON으로 내려줌
////        String responseBody = "{ \"access\": \"" + access + "\" }";
////        response.getWriter().write(responseBody);
//    }
//}

//@Override
//public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//
//    CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
//    String username = customUserDetails.getUsername();
//    String role = authentication.getAuthorities().iterator().next().getAuthority();
//
//    Map<String, String> tokens = tokenService.createTokens(username, role);
//    String access = tokens.get("access");
//    String refresh = tokens.get("refresh");
//
//    // Access Token → 헤더
//    response.setHeader("Authorization", "Bearer " + access);
//
//    // Refresh Token → ResponseCookie로 보내기
//    ResponseCookie refreshCookie = tokenService.createResponseCookie("refresh", refresh);
//    response.setHeader("Set-Cookie", refreshCookie.toString());
//
//    // 응답 body에 사용자 정보 보내기
//    response.setContentType("application/json;charset=UTF-8");
//    response.setStatus(HttpStatus.OK.value());
//
//    UserInfoDto userInfoDto = customOAuth2UserService.getNameAndCreatedAt(username);
//    response.getWriter().write(objectMapper.writeValueAsString(userInfoDto));
//    }
//}

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

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
//        response.sendRedirect("https://iotl-fe.vercel.app/");
        response.sendRedirect("https://localhost:5137/");
    }
}
