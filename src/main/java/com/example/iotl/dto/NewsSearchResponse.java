// NewsSearchResponse.java
package com.example.iotl.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewsSearchResponse {
    private int totalCount;
    private List<NewsDto> articles;
}