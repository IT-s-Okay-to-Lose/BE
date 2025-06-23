package com.example.iotl.filter;

import com.example.iotl.dto.CustomOAuth2User;
import com.example.iotl.dto.UserDto;
import com.example.iotl.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        // 헤더에서 access키에 담긴 토큰을 꺼냄
//        String accessToken = request.getHeader("access");
//
//        // 토큰이 없다면 다음 필터로 넘김
//        if (accessToken == null) {
//
//            filterChain.doFilter(request, response);
//
//            return;
//        }
//
//        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
//        try {
//            jwtUtil.isExpired(accessToken);
//        } catch (ExpiredJwtException e) {
//
//            //response body
//            PrintWriter writer = response.getWriter();
//            writer.print("access token expired");
//
//            //response status code
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return;
//        }
//
//        // 토큰이 access 인지 확인 (발급시 페이로드에 명시)
//        String category = jwtUtil.getCategory(accessToken);
//
//        if (!category.equals("access")) {
//
//            //response body
//            PrintWriter writer = response.getWriter();
//            writer.print("invalid access token");
//
//            //response status code
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return;
//        }
//
//        // username, role 값을 획득
//        String username = jwtUtil.getUsername(accessToken);
//        String role = jwtUtil.getRole(accessToken);
//
//        UserDto userDto = new UserDto();
//        userDto.setUsername(username);
//        userDto.setRole(role);
//        CustomOAuth2User customUserDetails = new CustomOAuth2User(userDto);
//
//        Authentication authToken = new UsernamePasswordAuthenticationToken( //인증서 발급
//                customUserDetails //사용자 정보
//                , null //비밀번호(이미 토큰 검정 끝났기 때문에 null 가능)
//                , customUserDetails.getAuthorities()); //권한 정보(ROLE_USER 등)
//
//        SecurityContextHolder.getContext().setAuthentication(authToken); //인증서를 Spring Security 전역 보안 컨텍스트에 공표
//
//        filterChain.doFilter(request, response);
//    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String header = request.getHeader("Authorization");
//
//        // 헤더가 없거나 Bearer로 시작하지 않으면 다음 필터로 진행
//        if (header == null || !header.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // "Bearer " 접두사 제거 후 토큰만 추출
//        String accessToken = header.substring(7);
//
//        try {
//            jwtUtil.isExpired(accessToken);
//        } catch (ExpiredJwtException e) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().print("access token expired");
//            return;
//        }
//
//        if (!"access".equals(jwtUtil.getCategory(accessToken))) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().print("invalid access token");
//            return;
//        }
//
//        String username = jwtUtil.getUsername(accessToken);
//        String role = jwtUtil.getRole(accessToken);
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

        // Spring Security에서 permitAll로 지정한 경로는 인증 스킵
        if (isPermitAllPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        String accessToken = header.substring(7);

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
                || uri.startsWith("/v3/api-docs/")
                || uri.startsWith("/swagger-ui/")
                || uri.equals("/swagger-ui.html")
                || uri.equals("/api/v1/news/top3");

    }
}