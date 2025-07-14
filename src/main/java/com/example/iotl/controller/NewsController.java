package com.example.iotl.controller;

import java.util.List;
import com.example.iotl.dto.news.NewsDto;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*") // 또는 허용할 도메인만 지정
@Tag(name = "News API", description = "뉴스 관련 API (Naver News OpenAPI 기반)")
@RestController
@RequestMapping("/auth/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final BaseResponseService baseResponseService;
    @Operation(
            summary = "랜덤 뉴스 3개 조회",
            description = "네이버 뉴스에서 최근 뉴스 중 랜덤으로 3개를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뉴스 3건 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/top3")
    public ResponseEntity<BaseResponse<List<NewsDto>>> getTop3() {
        List<NewsDto> articles = newsService.getTop3RandomNews().getArticles();
        BaseResponse<List<NewsDto>> response = baseResponseService.getSuccessResponse(articles);
        return ResponseEntity.ok(response);
    }
}