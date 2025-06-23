package com.example.iotl.controller;

import com.example.iotl.dto.marketindex.MarketIndexDto;
import com.example.iotl.entity.MarketIndex;
import com.example.iotl.scheduler.MarketIndexScheduler;
import com.example.iotl.service.MarketIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/market-index")
@RequiredArgsConstructor
public class MarketIndexController {

    private final MarketIndexService marketIndexService;

    // 오늘자 지수 조회 (프론트 요청용)
    @GetMapping("/{marketType}")
    public ResponseEntity<MarketIndexDto> getMarketIndex(@PathVariable String marketType) {
        try {
            MarketIndexDto dto = marketIndexService.getMarketIndex(marketType)
                    .map(responseDto -> {
                        var output = responseDto.getOutput();
                        MarketIndexDto result = new MarketIndexDto();
                        result.setIndexName(marketType.equalsIgnoreCase("KOSPI") ? "코스피" : "코스닥");
                        result.setCurrentValue(output.getBstp_nmix_prpr());
                        result.setChangeAmount(output.getBstp_nmix_prdy_vrss());
                        result.setChangeRate(output.getBstp_nmix_prdy_ctrt());
                        result.setChangeDirection("1".equals(output.getPrdy_vrss_sign()) ? "▲" : "▼");
                        return result;
                    })
                    .block(); // Mono → 동기 처리

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // 혹은 404로 설정해도 됨
        }
    }

    // 테스트: 강제로 외부 API 호출해서 저장
    @PostMapping("/test/trigger")
    public ResponseEntity<String> triggerMarketIndexScheduler() {
        marketIndexService.saveMarketIndex("KOSPI");
        marketIndexService.saveMarketIndex("KOSDAQ");
        return ResponseEntity.ok("Market index scheduler executed!");
    }
}