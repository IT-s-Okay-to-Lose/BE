package com.example.iotl.service;

import com.example.iotl.config.NaverApiConfig;
import com.example.iotl.dto.NaverNewsResponse;
import com.example.iotl.dto.NewsDto;
import com.example.iotl.dto.NewsSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NewsService {

    private final WebClient naverWebClient;
    private final NaverApiConfig naverApiConfig;

    public NewsSearchResponse getNews2(String keyword) {
        URI uri = UriComponentsBuilder
                .fromUriString(naverApiConfig.getNewsUrl())
                .queryParam("query", keyword)
                .queryParam("display", 20)
                .queryParam("start", 1)
                .queryParam("sort", "date")
                .build()
                .encode()
                .toUri();

        NaverNewsResponse naverNewsResponse = naverWebClient.get()
                .uri(uri)
                .header("X-Naver-Client-Id", naverApiConfig.getClientId()) // 직접 붙이기
                .header("X-Naver-Client-Secret", naverApiConfig.getClientSecret())
                .retrieve()
                .bodyToMono(NaverNewsResponse.class)
                .block();

        List<NewsDto> articles = naverNewsResponse.getItems().stream()
                .map(item -> new NewsDto(item.getTitle(), item.getLink(), item.getPubDate()))
                .toList();

        NewsSearchResponse response = new NewsSearchResponse();
        response.setTotalCount(naverNewsResponse.getTotal());
        response.setArticles(articles);
        return response;
    }
}