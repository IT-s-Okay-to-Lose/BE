package com.example.iotl.service;

import com.example.iotl.dto.hoga.HogaDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HogaCacheRefresher {

    private final HogaService hogaService;
    private final HogaRedisService hogaRedisService;

    /** DB의 PENDING/부분체결 주문을 가격대별로 집계해서 Redis 캐시에 전체 교체 저장 */
    public void refreshFromDb(String stockCode) {
        List<HogaDto> hogas = hogaService.getHogas(stockCode); // 이미 실주문 집계
        hogaRedisService.saveHoga(stockCode, hogas);           // 캐시 교체
    }
}
