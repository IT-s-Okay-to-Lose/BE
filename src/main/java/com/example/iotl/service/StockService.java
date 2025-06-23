package com.example.iotl.service;

import com.example.iotl.dto.stocks.StaticStockMetaDto;
import com.example.iotl.dto.stocks.StockPriceDto;
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
import java.util.*;
import java.util.stream.Collectors;

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

    // ✅ 토큰 발급
    public String getAccessToken() {
        if (accessToken != null) return accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/oauth2/tokenP", request, Map.class);
        accessToken = (String) response.getBody().get("access_token");

        return accessToken;
    }

    // ✅ 실시간 주식 데이터 조회
    public Map<String, Object> getStockPrice(String code) {
        getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("appKey", appKey);
        headers.set("appSecret", appSecret);
        headers.set("tr_id", "FHKST01010100");
        headers.set("custtype", "P");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-price")
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", code);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }

    // ✅ 주식 상세 정보 저장
    public StockPriceDto saveStockPrice(String code) {
        Map result = getStockPrice(code);
        Map<String, String> output = (Map<String, String>) result.get("output");

        Stocks stocks = stockInfoRepository.findById(code).orElse(null);
        if (stocks == null) return null;

        StockDetail stock = StockDetail.builder()
                .stocks(stocks)
                .stockCode(output.get("stck_shrn_iscd"))
                .openPrice(new BigDecimal(output.get("stck_oprc")))
                .highPrice(new BigDecimal(output.get("stck_hgpr")))
                .lowPrice(new BigDecimal(output.get("stck_lwpr")))
                .closePrice(new BigDecimal(output.get("stck_prpr")))
                .priceDiff(new BigDecimal(output.get("prdy_vrss")))
                .priceRate(new BigDecimal(output.get("prdy_ctrt")))
                .priceSign(Byte.parseByte(output.get("prdy_vrss_sign")))
                .volume(Long.parseLong(output.get("acml_vol")))
                .prevClosePrice(new BigDecimal(output.get("stck_prpr")).subtract(new BigDecimal(output.get("prdy_vrss"))))
                .createdAt(LocalDateTime.now())
                .build();

        StockDetail saved = stockRepository.save(stock);
        return new StockPriceDto(saved);
    }

    // ✅ 종목 코드 전체 조회 (캔들, 거래량 등)
    public List<StockDetail> findStocksByCode(String code) {
        return stockRepository.findByStockCode(code);
    }

    // ✅ 종목 코드로 가장 최신 1건 조회
    public StockDetail findLatestStockByCode(String code) {
        return stockRepository.findTop1ByStockCodeOrderByCreatedAtDesc(code);
    }

    // ✅ 전체 종목 코드 리스트 조회
    public List<String> getAllStockCodes() {
        return stockInfoRepository.findAllStockCodes();
    }

    // ✅ 모든 상세 데이터 (사용 주의)
    public List<StockDetail> findAllStocks() {
        return stockRepository.findAll();
    }

    // ✅ 정적 정보 조회
    public List<StaticStockMetaDto> getAllStockMetas() {
        return stockInfoRepository.findAll().stream()
                .map(StaticStockMetaDto::new)
                .collect(Collectors.toList());
    }

    // ✅ 여러 종목 코드 → 최신 데이터 리스트로 변환
    public List<StockDetail> findLatestStocksByCodes(List<String> codes) {
        return codes.stream()
                .map(this::findLatestStockByCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<StockDetail> findTodayStocksByCode(String code) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        return stockRepository.findByStockCodeAndCreatedAtBetween(code, startOfDay, now);
    }

    // 여기서 그거 받아온거 MarketIndexServ로 넘기는거임
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }
}