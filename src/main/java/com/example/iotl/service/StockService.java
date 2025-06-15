package com.example.iotl.service;

import com.example.iotl.dto.StockPriceDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.Stocks;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockService {

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;
    private final StockInfoRepository stockInfoRepository;

    @Value("${kis.api.base-url}")
    private String baseUrl;

    @Value("${kis.api.appkey}")
    private String appKey;

    @Value("${kis.api.appsecret}")
    private String appSecret;

    private String accessToken;

    /**
     * 액세스 토큰 가져오기
     */
    public String getAccessToken() {
        if (accessToken != null) return accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/oauth2/tokenP", request, Map.class
        );
        accessToken = (String) response.getBody().get("access_token");
        return accessToken;
    }

    /**
     * 종목 코드에 대한 실시간 가격 정보 요청
     */
    public Map<String, Object> getStockPrice(String code) {
        getAccessToken(); // 토큰 미리 획득

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("appKey", appKey);
        headers.set("appSecret", appSecret);
        headers.set("tr_id", "FHKST01010100");
        headers.set("custtype", "P");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-price")
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", code);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, Map.class
        );

        return response.getBody();
    }

    /**
     * 실시간 가격 저장 및 DTO 반환
     */
    public StockPriceDto saveStockPrice(String code) {
        Map result = getStockPrice(code);
        Map<String, String> output = (Map<String, String>) result.get("output");

        Stocks stocks = stockInfoRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("❌ 등록되지 않은 종목 코드: " + code));

        StockDetail stock = StockDetail.builder()
                .stockCode(output.get("stck_shrn_iscd"))
                .stocks(stocks) // Join 설정
                .openPrice(new BigDecimal(output.get("stck_oprc")))
                .highPrice(new BigDecimal(output.get("stck_hgpr")))
                .lowPrice(new BigDecimal(output.get("stck_lwpr")))
                .closePrice(new BigDecimal(output.get("stck_prpr")))
                .priceDiff(new BigDecimal(output.get("prdy_vrss")))
                .priceRate(new BigDecimal(output.get("prdy_ctrt")))
                .priceSign(Byte.parseByte(output.get("prdy_vrss_sign")))
                .volume(Long.parseLong(output.get("acml_vol")))
                .prevClosePrice(new BigDecimal(output.get("stck_prpr"))
                        .subtract(new BigDecimal(output.get("prdy_vrss"))))
                .createdAt(LocalDateTime.now())
                .build();

        StockDetail saved = stockRepository.save(stock);
        return new StockPriceDto(saved);
    }

    /**
     * 종목 코드 전체 조회
     */
    public List<String> getAllStockCodes() {
        return stockInfoRepository.findAllStockCodes();
    }
}