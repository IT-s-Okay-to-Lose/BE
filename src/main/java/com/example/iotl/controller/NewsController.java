package com.example.iotl.controller;

import java.util.List;
import com.example.iotl.dto.NewsDto;
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
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/top3")
    public ResponseEntity<List<NewsDto>> getTop3() {
        return ResponseEntity.ok(newsService.getTop3RandomNews().getArticles());
    }
}