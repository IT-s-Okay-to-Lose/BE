package com.example.iotl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "naver")
@Getter
@Setter
public class NaverApiConfig {
    private String clientId;
    private String clientSecret;
    private String newsUrl;
}