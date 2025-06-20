package com.example.iotl.service;

import com.example.iotl.config.NaverApiConfig;
import com.example.iotl.dto.news.NaverNewsResponse;
import com.example.iotl.dto.news.NewsDto;
import com.example.iotl.dto.news.NewsSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final WebClient naverWebClient;
    private final NaverApiConfig naverApiConfig;

    // 언론사 매핑
    private static final Map<String, String> PRESS_NAME_MAP = Map.ofEntries(
        Map.entry("edaily", "이데일리"),
        Map.entry("yna", "연합뉴스"),
        Map.entry("joins", "중앙일보"),
        Map.entry("khan", "경향신문"),
        Map.entry("hankyoreh", "한겨레"),
        Map.entry("h21", "한겨레21"),
        Map.entry("donga", "동아일보"),
        Map.entry("chosun", "조선일보"),
        Map.entry("mbn", "MBN"),
        Map.entry("news", "네이버뉴스"),
        Map.entry("allurekorea", "얼루어코리아")
        // Add more as needed
    );

    public NewsSearchResponse getTop3RandomNews() {
        String keyword = "뉴스";

        URI uri = UriComponentsBuilder
                .fromUriString(naverApiConfig.getNewsUrl())
                .queryParam("query", keyword)
                .queryParam("display", 20)
                .queryParam("start", 1)
                .queryParam("sort", "date")
                .build(false)
                .encode()
                .toUri();

        NaverNewsResponse response = naverWebClient.get()
                .uri(uri)
                .header("X-Naver-Client-Id", naverApiConfig.getClientId())
                .header("X-Naver-Client-Secret", naverApiConfig.getClientSecret())
                .retrieve()
                .bodyToMono(NaverNewsResponse.class)
                .block();

        if (response == null || response.getItems() == null) {
            return new NewsSearchResponse(0, List.of());
        }

        List<NewsDto> articles = response.getItems().stream()
                .filter(item -> item.getTitle() != null)
                .map(item -> {
                    String cleanedTitle = cleanHtml(item.getTitle());
                    return new NewsDto(
                            UUID.randomUUID().toString(),
                            cleanedTitle,
                            extractPressFromLink(item.getOriginallink())
                    );
                })
                .collect(Collectors.toList());

        Collections.shuffle(articles);
        List<NewsDto> random3 = articles.stream().limit(3).toList();

        return new NewsSearchResponse(random3.size(), random3);
    }

    private String cleanHtml(String text) {
        return text.replaceAll("<.*?>", "").replaceAll("&quot;", "\"");
    }

    private String extractPressFromLink(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null && host.contains(".")) {
                String[] parts = host.replace("www.", "").split("\\.");
                for (String part : parts) {
                    if (PRESS_NAME_MAP.containsKey(part)) {
                        return PRESS_NAME_MAP.get(part);
                    }
                }
                return parts[0]; // fallback to first part of host
            }
        } catch (Exception e) {
            log.warn("언론사 추출 실패: {}", url);
        }
        return "언론사 미확인";
    }
}