package com.example.iotl.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "security")
public class PermitAllPathProperties {
    private List<String> permitAllPaths = List.of();

    public List<String> getPermitAllPaths() {
        for(String str : permitAllPaths) {
            System.out.println("@@@@@@" + str);
        }
        return permitAllPaths;
    }

    public void setPermitAllPaths(List<String> permitAllPaths) {
        this.permitAllPaths = permitAllPaths;
    }
}
