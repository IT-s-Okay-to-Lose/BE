package com.example.iotl.controller;

import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.service.HogaCacheRefresher;
import com.example.iotl.service.HogaRedisService;
import com.example.iotl.service.HogaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/api/hoga")
public class HogaController {

    private final HogaService hogaService;
    private final HogaRedisService hogaRedisService;
    private final HogaCacheRefresher hogaCacheRefresher;
    private final BaseResponseService baseResponseService;

    /** 캐시 강제 리프레시 — DB 집계 -> Redis 저장 */
    @PostMapping("/refresh/{stockCode}")
    public ResponseEntity<BaseResponse<String>> refresh(@PathVariable String stockCode) {
        try {
            hogaCacheRefresher.refreshFromDb(stockCode);
            return ResponseEntity.ok(baseResponseService.getSuccessResponse("OK"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(baseResponseService.getFailureResponse("호가 리프레시 실패: " + e.getMessage(), 500));
        }
    }

    /** 조회: Redis 우선, 없으면 DB 집계 -> 캐시 저장 -> 응답 */
    @GetMapping("/{stockCode}")
    public ResponseEntity<BaseResponse<List<HogaDto>>> getHoga(@PathVariable String stockCode) {
        List<HogaDto> cached = hogaRedisService.getHoga(stockCode);
        if (cached != null && !cached.isEmpty()) {
            return ResponseEntity.ok(baseResponseService.getSuccessResponse(cached));
        }
        List<HogaDto> fromDb = hogaService.getHogas(stockCode);
        hogaRedisService.saveHoga(stockCode, fromDb);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(fromDb));
    }

    // 자동 생성/스케줄러 관련 API는 제거(또는 주석)
}
