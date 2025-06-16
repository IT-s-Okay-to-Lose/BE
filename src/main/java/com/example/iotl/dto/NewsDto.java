package com.example.iotl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

// 프론트에 요청 dto
@Data
@AllArgsConstructor
public class NewsDto {
    @Schema(description = "뉴스 제목", example = "국민은행 주가 상승")
    private String id;
    @Schema(description = "뉴스 기사 링크", example = "https://example.com/news/1")
    private String title;
    @Schema(description = "뉴스 발행 날짜", example = "2025-06-16")
    private String press;
}