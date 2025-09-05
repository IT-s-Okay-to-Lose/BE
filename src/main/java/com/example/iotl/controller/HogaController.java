package com.example.iotl.controller;

import com.example.iotl.domain.hoga.HogaGenerator;
import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.service.HogaRedisService;
import com.example.iotl.service.HogaService;
import com.example.iotl.service.HogaCacheRefresher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/api/hoga")
public class HogaController {

    private final HogaService hogaService;
    private final HogaRedisService hogaRedisService;
    private final HogaCacheRefresher hogaCacheRefresher; // 추가

    /**
     * 캐시 강제 리프레시(운영편의용) — DB 집계 -> Redis 저장
     */
    @PostMapping("/refresh/{stockCode}")
    public String refresh(@PathVariable String stockCode) {
        hogaCacheRefresher.refreshFromDb(stockCode);
        return "OK";
    }

    /**
     * 조회: Redis 우선, 없으면 DB 집계 -> 캐시 저장 -> 응답
     */
    @GetMapping("/{stockCode}")
    public List<HogaDto> getHoga(@PathVariable String stockCode) {
        List<HogaDto> cached = hogaRedisService.getHoga(stockCode);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<HogaDto> fromDb = hogaService.getHogas(stockCode); // 실주문 집계
        hogaRedisService.saveHoga(stockCode, fromDb);
        return fromDb;
    }

    // 🔥 삭제(또는 주석처리): 자동 생성/오토필/벌크 초기화
    // @GetMapping("/fill") ...
    // @PostMapping("/bulk-init") ...
}
