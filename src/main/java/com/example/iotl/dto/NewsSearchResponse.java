// NewsSearchResponse.java
package com.example.iotl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NewsSearchResponse {
    private int totalCount;
    private List<NewsDto> articles;
}