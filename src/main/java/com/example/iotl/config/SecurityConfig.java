package com.example.iotl.config;

import com.example.iotl.filter.JWTFilter;
import com.example.iotl.handler.CustomSuccessHandler;
import com.example.iotl.jwt.JWTUtil;
import com.example.iotl.service.security.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomSuccessHandler customSuccessHandler) throws Exception {

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //JWTFilter 추가
        http
                .addFilterAfter(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler));

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                            .requestMatchers(
                                    "/", "/login", "/oauth2/**", "/login/oauth2/**", "/reissue",
                                    "/ws/**",                       // WebSocket 허용
                                    "/api/stocks/**",
                                    "/api/stocks/meta",            // 정적 주식 데이터
                                    "/api/stocks/dynamic",         // 동적 주식 데이터
                                    "/api/stocks/chart/**",       // 차트 관련 주식 데이터 (있다면)
                                    "/api/exchange",               // 환율 데이터
                                    "/api/market-index/**",             // 지수 데이터
                                    "/api/v1/news/top3", //뉴스
                                    "/v3/api-docs/**", // swagger
                                    "/swagger-ui/**", // swagger
                                    "/swagger-ui.html" // swagger
                            ).permitAll()
                            .anyRequest().authenticated());
//        http
//                .authorizeHttpRequests((auth) -> auth
//                        .anyRequest().permitAll());

        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(List.of(
                                "http://127.0.0.1:5500",
                                "http://localhost:8080",
                                "https://iotl-fe.vercel.app",
                                "https://localhost:5173",
                                "http://localhost:5173",
                                "https://localhost:5137",
                                "http://localhost:5137"
                        ));
                        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        configuration.setAllowedHeaders(List.of("*"));
                        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
                        configuration.setAllowCredentials(true);
                        configuration.setMaxAge(3600L);

                        return configuration;
                    }
                }));

        return http.build();
    }
}
