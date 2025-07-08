package com.example.iotl.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConfigurationProperties(prefix = "permit-all-paths")
public class PermitAllPathProperties {
    private List<String> paths = List.of();

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
