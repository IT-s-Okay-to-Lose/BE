package com.example.iotl.dto.news;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

// 프론트에 요청 dto
@Schema(description = "뉴스 정보 DTO")
@Data
@AllArgsConstructor
public class NewsDto {
    @Schema(description = "뉴스 ID", example = "14a5ad9f-4f51-46ad-bd9d-35de35304207")
    private String id;
    @Schema(description = "뉴스 제목", example = "코스피 2,700선 돌파... 외국인 매수세 유입")
    private String title;
    @Schema(description = "언론사", example = "이데일리")
    private String press;
}