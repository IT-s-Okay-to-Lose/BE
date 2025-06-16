package com.example.iotl.controller;

import com.example.iotl.dto.NewsSearchResponse;
import com.example.iotl.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;

    // http://localhost:8080/api/v1/news/search?keyword=삼성전자
    @GetMapping("/search")
    public ResponseEntity<NewsSearchResponse> getNews(@RequestParam("keyword") String keyword) {
        System.out.println("controller 들어옴");
        NewsSearchResponse response = newsService.getNews2(keyword);
        return ResponseEntity.ok(response);
    }
}