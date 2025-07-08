package com.example.iotl.controller;

import com.example.iotl.dto.marketindex.MarketIndexDto;
import com.example.iotl.service.marketIndex.MarketIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/market-index")
@RequiredArgsConstructor
public class MarketIndexController {

    private final MarketIndexService marketIndexService;

    @GetMapping("/{marketType}")
    public ResponseEntity<MarketIndexDto> getToday(@PathVariable String marketType) {
        MarketIndexDto dto = marketIndexService.getToday(marketType);

        if (dto == null) {
            boolean success = marketIndexService.saveIfAbsent(marketType);
            if (!success) return ResponseEntity.status(502).build(); // 외부 API 실패
            dto = marketIndexService.getToday(marketType);
            if (dto == null) return ResponseEntity.noContent().build(); // 저장 실패
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/save")
    public ResponseEntity<String> forceSave() {
        boolean kospi = marketIndexService.saveIfAbsent("KOSPI");
        boolean kosdaq = marketIndexService.saveIfAbsent("KOSDAQ");
        return ResponseEntity.ok("저장 완료: " + (kospi || kosdaq));
    }
}