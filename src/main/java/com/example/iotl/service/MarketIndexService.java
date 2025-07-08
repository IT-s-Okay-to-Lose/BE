package com.example.iotl.service;

import com.example.iotl.dto.marketindex.CurrentIndexResponseDto;
import com.example.iotl.dto.marketindex.MarketIndexDto;
import com.example.iotl.entity.MarketIndex;
import com.example.iotl.repository.MarketIndexRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MarketIndexService {
    private final StockService stockService;
    private final WebClient.Builder webClientBuilder;
    private final MarketIndexRepository marketIndexRepository;

    public Mono<CurrentIndexResponseDto> getMarketIndex(String marketType) {
        String code;
        String indexName;

        switch (marketType.toUpperCase()) {
            case "KOSPI":
                code = "0001";
                indexName = "코스피";
                break;
            case "KOSDAQ":
                code = "1001";
                indexName = "코스닥";
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 marketType: " + marketType);
        }

        String accessToken = stockService.getAccessToken();
        WebClient webClient = webClientBuilder
                .baseUrl(stockService.getBaseUrl())  // ✅ baseUrl 가져오기
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/volume-rank")
                        .queryParam("FID_COND_MRKT_DIV_CODE", "U")
                        .queryParam("FID_INPUT_ISCD", code)
                        .build())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("tr_id", "FHPUP02100000")
                .header("custtype", "P")
                .header("appkey", stockService.getAppKey())       // ✅ 가져오기
                .header("appsecret", stockService.getAppSecret()) // ✅ 가져오기
                .header("authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(CurrentIndexResponseDto.class);
    }

    // ★ DB 저장용 메서드
    public MarketIndex saveMarketIndex(MarketIndexDto dto) {
        MarketIndex entity = MarketIndex.builder()
                .indexName(dto.getIndexName())
                .currentValue(dto.getCurrentValue())
                .changeAmount(dto.getChangeAmount())
                .changeRate(dto.getChangeRate())
                .changeDirection(dto.getChangeDirection())
                .date(LocalDate.now())
                .build();
        return marketIndexRepository.save(entity);
    }

    public MarketIndexDto getTodayMarketIndexFromDb(String marketType) {
        String name = marketType.equalsIgnoreCase("KOSPI") ? "코스피" : "코스닥";
        return marketIndexRepository.findByIndexNameAndDate(name, LocalDate.now())
                .map(index -> {
                    MarketIndexDto dto = new MarketIndexDto();
                    dto.setIndexName(index.getIndexName());
                    dto.setCurrentValue(index.getCurrentValue());
                    dto.setChangeAmount(index.getChangeAmount());
                    dto.setChangeRate(index.getChangeRate());
                    dto.setChangeDirection(index.getChangeDirection());
                    return dto;
                }).orElse(null);
    }

    // 외부 API에서 받아서 저장까지 처리하는 메서드
    public void saveMarketIndex(String marketType) {
        String indexName = marketType.equals("KOSPI") ? "코스피" : "코스닥";

        // ✅ 이미 저장되어 있다면 스킵
        boolean exists = marketIndexRepository.existsByIndexNameAndDate(indexName, LocalDate.now());
        if (exists) {
            return;
        }

        getMarketIndex(marketType)
                .map(responseDto -> {
                    var output = responseDto.getOutput();

                    MarketIndexDto dto = new MarketIndexDto();
                    dto.setIndexName(indexName);
                    dto.setCurrentValue(output.getBstp_nmix_prpr());
                    dto.setChangeAmount(output.getBstp_nmix_prdy_vrss());
                    dto.setChangeRate(output.getBstp_nmix_prdy_ctrt());
                    dto.setChangeDirection("1".equals(output.getPrdy_vrss_sign()) ? "▲" : "▼");

                    return dto;
                })
                .doOnNext(this::saveMarketIndex)
                .subscribe();
    }

    // Mono를 subscribe 하지 않고 block()으로 바로 받는 방식
    public void saveMarketIndexBlocking(String marketType) {
        String indexName = marketType.equalsIgnoreCase("KOSPI") ? "코스피" : "코스닥";

        boolean exists = marketIndexRepository.existsByIndexNameAndDate(indexName, LocalDate.now());
        if (exists) return;

        try {
            MarketIndexDto dto = getMarketIndex(marketType)
                    .map(responseDto -> {
                        var output = responseDto.getOutput();

                        MarketIndexDto result = new MarketIndexDto();
                        result.setIndexName(indexName);
                        result.setCurrentValue(output.getBstp_nmix_prpr());
                        result.setChangeAmount(output.getBstp_nmix_prdy_vrss());
                        result.setChangeRate(output.getBstp_nmix_prdy_ctrt());
                        result.setChangeDirection("1".equals(output.getPrdy_vrss_sign()) ? "▲" : "▼");

                        return result;
                    })
                    .block(); // ✅ 여기서 동기적으로 기다림

            if (dto != null) {
                saveMarketIndex(dto); // DB 저장
            }
        } catch (Exception e) {
            //log.error("❌ 지수 저장 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}