package com.example.iotl.service;

import com.example.iotl.config.NaverApiConfig;
import com.example.iotl.dto.NaverNewsResponse;
import com.example.iotl.dto.NewsDto;
import com.example.iotl.dto.NewsSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final WebClient naverWebClient;
    private final NaverApiConfig naverApiConfig;

    public NewsSearchResponse getNews2(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            URI uri = UriComponentsBuilder
                    .fromUriString(naverApiConfig.getNewsUrl())
                    .queryParam("query", encodedKeyword)
                    .queryParam("display", 20)
                    .queryParam("start", 1)
                    .queryParam("sort", "date")
                    .build(false)
                    .encode()
                    .toUri();

            NaverNewsResponse naverNewsResponse = naverWebClient.get()
                    .uri(uri)
                    .header("X-Naver-Client-Id", naverApiConfig.getClientId())
                    .header("X-Naver-Client-Secret", naverApiConfig.getClientSecret())
                    .retrieve()
                    .bodyToMono(NaverNewsResponse.class)
                    .block();

            if (naverNewsResponse == null || naverNewsResponse.getItems() == null) {
                log.warn("네이버 뉴스 응답이 null 또는 items가 없음: {}", naverNewsResponse);
                return new NewsSearchResponse(0, Collections.emptyList());
            }

            List<NewsDto> articles = naverNewsResponse.getItems().stream()
                    .map(item -> new NewsDto(item.getTitle(), item.getLink(), item.getPubDate()))
                    .toList();

            return new NewsSearchResponse(naverNewsResponse.getTotal(), articles);

        } catch (Exception e) {
            log.error("뉴스 검색 중 예외 발생: {}", e.getMessage(), e);
            return new NewsSearchResponse(0, Collections.emptyList());
        }
    }
}