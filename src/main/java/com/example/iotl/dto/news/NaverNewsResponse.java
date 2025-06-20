package com.example.iotl.dto.news;

import lombok.Data;

import java.util.List;

@Data
public class NaverNewsResponse {
    private String lastBuildDate; // 검색 결과를 생성한 시간
    private int total; // 총 검색 결과 개수
    private int start; // 검색 시작 위치
    private int display; //한 번에 표시할 검색 결과 개수
    private List<NaverNewsItem> items;

    @Data
    public static class NaverNewsItem {
        private String title; // 뉴스 기사의 제목
        private String originallink;
        private String link;
        private String description;
        private String pubDate;
    }
}