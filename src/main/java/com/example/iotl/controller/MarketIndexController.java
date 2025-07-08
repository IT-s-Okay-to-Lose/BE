package com.example.iotl.controller;

import com.example.iotl.dto.marketindex.MarketIndexDto;
import com.example.iotl.entity.MarketIndex;
import com.example.iotl.scheduler.MarketIndexScheduler;
import com.example.iotl.service.MarketIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/market-index")
@RequiredArgsConstructor
public class MarketIndexController {

    private final MarketIndexService marketIndexService;

    // 오늘자 지수 조회 (프론트 요청용)
    @GetMapping("/{marketType}")
    public ResponseEntity<MarketIndexDto> getMarketIndex(@PathVariable String marketType) {
        log.info("📥 지수 요청 들어옴: {}", marketType);

        try {
            MarketIndexDto dto = marketIndexService.getTodayMarketIndexFromDb(marketType);

            if (dto == null) {
                // 외부 API 호출 후 저장 시도 (비동기 → 동기화 필요)
                marketIndexService.saveMarketIndexBlocking(marketType); // ← 여기에 동기 저장 메서드 필요
                // 다시 DB 조회
                dto = marketIndexService.getTodayMarketIndexFromDb(marketType);
                if (dto == null) {
                    return ResponseEntity.noContent().build();
                }
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("❌ 지수 요청 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // 테스트: 강제로 외부 API 호출해서 저장
    @PostMapping("/save")
    public ResponseEntity<String> triggerMarketIndexScheduler() {
        marketIndexService.saveMarketIndex("KOSPI");
        marketIndexService.saveMarketIndex("KOSDAQ");
        return ResponseEntity.ok("Market index scheduler executed!");
    }
}