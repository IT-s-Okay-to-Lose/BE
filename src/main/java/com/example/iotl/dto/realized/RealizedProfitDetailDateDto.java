package com.example.iotl.dto.realized;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Schema(description = "특정 날짜의 실현 수익 상세 리스트 DTO")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RealizedProfitDetailDateDto {
    @Schema(description = "날짜 (yyyy-MM-dd 형식)", example = "2025-06-18")
    private String date;
    @Schema(description = "해당 날짜의 실현 수익 상세 항목 리스트")
    private List<RealizedProfitDetailDto> items;
}
