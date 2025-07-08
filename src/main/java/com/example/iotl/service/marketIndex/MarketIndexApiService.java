package com.example.iotl.service.marketIndex;

import com.example.iotl.dto.marketindex.CurrentIndexResponseDto;
import com.example.iotl.service.stock.StockApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class MarketIndexApiService {

    private final StockApiService stockApiService;
    private final WebClient.Builder webClientBuilder;

    public CurrentIndexResponseDto fetchIndex(String marketType) {
        String code = switch (marketType.toUpperCase()) {
            case "KOSPI" -> "0001";
            case "KOSDAQ" -> "1001";
            default -> throw new IllegalArgumentException("지원하지 않는 marketType: " + marketType);
        };

        String accessToken = stockApiService.getAccessToken();

        return webClientBuilder.baseUrl(stockApiService.getBaseUrl())
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/volume-rank")
                        .queryParam("FID_COND_MRKT_DIV_CODE", "U")
                        .queryParam("FID_INPUT_ISCD", code)
                        .build())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("tr_id", "FHPUP02100000")
                .header("custtype", "P")
                .header("appkey", stockApiService.getAppKey())
                .header("appsecret", stockApiService.getAppSecret())
                .header("authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(CurrentIndexResponseDto.class)
                .block();
    }
}