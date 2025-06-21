//package com.example.iotl.service;
//
//import com.example.iotl.entity.RefreshEntity;
//import com.example.iotl.jwt.JWTUtil;
//import com.example.iotl.repository.RefreshRepository;
//import jakarta.servlet.http.Cookie;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class TokenService {
//
//    private final JWTUtil jwtUtil;
//    private final RefreshRepository refreshRepository;
//
//    public TokenService(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
//        this.jwtUtil = jwtUtil;
//        this.refreshRepository = refreshRepository;
//    }
//
//    /**
//     * access + refresh 토큰을 생성하고 refresh 토큰은 DB에 저장
//     */
//    public Map<String, String> createTokens(String username, String role) {
//        String accessToken = jwtUtil.createJwt("access", username, role, 900000L); //15분
//        String refreshToken = jwtUtil.createJwt("refresh", username, role, 604800000L); //1주일
//
//        addRefreshEntity(username, refreshToken, 604800000L); //refresh토큰 저장
//
//        Map<String, String> tokenMap = new HashMap<>();
//        tokenMap.put("access", accessToken);
//        tokenMap.put("refresh", refreshToken);
//        return tokenMap;
//    }
//
//    /**
//     * HttpOnly 쿠키 생성
//     */
//    public Cookie createCookie(String key, String value) {
//        Cookie cookie = new Cookie(key, value);
//        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
//        cookie.setHttpOnly(true);
//        cookie.setPath("/"); //전체 경로에 쿠키 설정
//        cookie.setSecure(true); //HTTPS 전용 설정
//        return cookie;
//    }
//
//    //iotl.store
//
//    private void addRefreshEntity(String username, String refresh, Long expiredMs) {
//        Date expiration = new Date(System.currentTimeMillis() + expiredMs);
//
//        RefreshEntity refreshEntity = new RefreshEntity();
//        refreshEntity.setUsername(username);
//        refreshEntity.setRefresh(refresh);
//        refreshEntity.setExpiration(expiration.toString());
//
//        refreshRepository.save(refreshEntity);
//    }
//
//    /**
//     * 기존 refresh 삭제 후 새로운 refresh 저장
//     */
//    public Map<String, String> rotateRefreshToken(String username, String role, String oldRefreshToken, long accessExpiry, long refreshExpiry) {
//        // 기존 refresh 삭제
//        refreshRepository.deleteByRefresh(oldRefreshToken);
//
//        // 새 토큰 발급
//        String newAccessToken = jwtUtil.createJwt("access", username, role, accessExpiry);
//        String newRefreshToken = jwtUtil.createJwt("refresh", username, role, refreshExpiry);
//
//        addRefreshEntity(username, newRefreshToken, refreshExpiry);
//
//        Map<String, String> tokenMap = new HashMap<>();
//        tokenMap.put("access", newAccessToken);
//        tokenMap.put("refresh", newRefreshToken);
//        return tokenMap;
//    }
//}
