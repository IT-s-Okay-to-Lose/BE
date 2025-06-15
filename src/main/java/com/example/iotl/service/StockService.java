package com.example.iotl.service;

import com.example.iotl.dto.StockPriceDto;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.StockPrice;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.repository.StockPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
public class StockService {

    private final RestTemplate restTemplate;
    private final StockPriceRepository stockPriceRepository;

    @Value("${kis.api.base-url}")
    private String baseUrl;

    @Value("${kis.api.appkey}")
    private String appKey;

    @Value("${kis.api.appsecret}")
    private String appSecret;

    private String accessToken;

    public StockService(RestTemplate restTemplate, StockPriceRepository stockPriceRepository) {
        this.restTemplate = restTemplate;
        this.stockPriceRepository = stockPriceRepository;
    }
    @Autowired
    private StockInfoRepository stockInfoRepository;

    public List<String> getAllStockCodes() {
        return stockInfoRepository.findAll()
                .stream()
                .map(Stocks::getStockCode)
                .toList();
    }

    public String getAccessToken() {
        String url = baseUrl + "/oauth2/tokenP";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        accessToken = (String) response.getBody().get("access_token");
        return accessToken;
    }

    public Map getStockPrice(String code) {
        if (accessToken == null) {
            getAccessToken();
        }

        String url = baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-price";

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("appKey", appKey);
        headers.set("appSecret", appSecret);
        headers.set("tr_id", "FHKST01010100"); // 현재가 조회용
        headers.set("custtype", "P");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", code);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }

    public StockPriceDto saveStockPrice(String code) {
        Map result = getStockPrice(code);
        Map<String, String> output = (Map<String, String>) result.get("output");

        StockPrice stock = new StockPrice();
        stock.setStockCode(output.get("stck_shrn_iscd"));
        stock.setOpenPrice(new BigDecimal(output.get("stck_oprc")));
        stock.setHighPrice(new BigDecimal(output.get("stck_hgpr")));
        stock.setLowPrice(new BigDecimal(output.get("stck_lwpr")));
        stock.setClosePrice(new BigDecimal(output.get("stck_prpr")));
        stock.setPriceDiff(new BigDecimal(output.get("prdy_vrss")));
        stock.setPriceRate(new BigDecimal(output.get("prdy_ctrt")));
        stock.setPriceSign(Byte.parseByte(output.get("prdy_vrss_sign")));
        stock.setVolume(Long.parseLong(output.get("acml_vol")));
        stock.setPrevClosePrice(stock.getClosePrice().subtract(stock.getPriceDiff()));
        stock.setCreatedAt(LocalDateTime.now());

        StockPrice saved = stockPriceRepository.save(stock);
        return new StockPriceDto(saved);
    }
}