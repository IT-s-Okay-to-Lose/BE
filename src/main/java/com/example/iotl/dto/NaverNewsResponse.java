// NaverNewsResponse.java
package com.example.iotl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NaverNewsResponse {
    private int total;

    @JsonProperty("items")
    private List<NaverNewsItem> items;

    @Data
    public static class NaverNewsItem {
        private String title;
        private String originallink;
        private String link;
        private String description;
        private String pubDate;
    }
}