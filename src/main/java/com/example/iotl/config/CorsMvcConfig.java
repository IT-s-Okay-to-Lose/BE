package com.example.iotl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie")
                .allowedOrigins("https://iotl-fe.vercel.app") //프론트엔드 도메인으로 바꿔야함
                .allowedMethods("*")                // 모든 HTTP 메서드 허용 (GET, POST 등)
                .allowCredentials(true);           // 쿠키 포함 요청 허용 (credentials: 'include' 사용 시 필요)
    }
}
