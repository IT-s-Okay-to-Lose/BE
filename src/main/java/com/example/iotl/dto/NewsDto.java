package com.example.iotl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// 프론트에 요청 dto
@Data
@AllArgsConstructor
public class NewsDto {
    private String id;
    private String title;
    private String press;
}