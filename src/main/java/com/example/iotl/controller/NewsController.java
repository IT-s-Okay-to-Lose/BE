package com.example.iotl.controller;

import com.example.iotl.dto.NewsSearchResponse;
import com.example.iotl.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;

    @Operation(
            summary = "뉴스 검색",
            description = "키워드를 입력하면 관련 뉴스 기사들을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 뉴스 검색 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<NewsSearchResponse> getNews(
            @Parameter(description = "검색할 뉴스 키워드", example = "국민은행")
            @RequestParam("keyword") String keyword
    ) {
        NewsSearchResponse response = newsService.getNews2(keyword);
        return ResponseEntity.ok(response);
    }
}