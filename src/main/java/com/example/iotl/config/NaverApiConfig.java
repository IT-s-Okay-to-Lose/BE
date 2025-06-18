package com.example.iotl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "naver")
@Getter
@Setter
@EnableConfigurationProperties(NaverApiConfig.class)
public class NaverApiConfig {
    private String clientId;
    private String clientSecret;
    private String newsUrl;
}