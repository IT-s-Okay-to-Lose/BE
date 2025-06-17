package com.example.iotl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

// 프론트에 요청 dto
@Data
@AllArgsConstructor
public class NewsDto {
    @Schema(description = "아이디", example = "1")
    private String id;
    @Schema(description = "뉴스 제목", example = "코스피 2,700선 돌파... 외국인 매수세 유입")
    private String title;
    @Schema(description = "언론사", example = "이데일리")
    private String press;
}