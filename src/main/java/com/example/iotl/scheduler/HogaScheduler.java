//package com.example.iotl.scheduler;
//
//import com.example.iotl.dto.hoga.HogaDto;
//import com.example.iotl.repository.StocksRepository;
//import com.example.iotl.service.HogaAutoFillService;
//import com.example.iotl.service.HogaRedisService;
//import com.example.iotl.service.HogaService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class HogaScheduler {
//
//    private final StocksRepository stockInfoRepository;
//    private final HogaService hogaService;
//    private final HogaRedisService hogaRedisService;
//    private final HogaAutoFillService hogaAutoFillService;
//
//    @Scheduled(fixedRate = 1000000) // 100초마다 실행
//    public void updateHogaIfPriceChanged() {
//        List<String> stockCodes = stockInfoRepository.findAllStockCodes();
//
//        for (String stockCode : stockCodes) {
//            try {
//                // 1. 최신 현재가 가져오기
//                BigDecimal latestPrice = hogaService.getLatestClosePrice(stockCode);
//
//                // 2. Redis에 저장된 이전 현재가 조회
//                Optional<BigDecimal> previousPriceOpt = hogaRedisService.getLastPrice(stockCode);
//
//                // 3. 현재가가 다르면 호가 regenerate
//                if (previousPriceOpt.isEmpty() || previousPriceOpt.get().compareTo(latestPrice) != 0) {
//                    List<HogaDto> newHogas = hogaService.getHogas(stockCode); // regenerate
//                    hogaRedisService.saveHoga(stockCode, newHogas);          // Redis에 저장
//                    hogaRedisService.saveCurrentPrice(stockCode, latestPrice); // 현재가도 저장
//
//                    // ⏬ 호가 부족 여부 확인 → 부족하면 주문 추가
//                    hogaAutoFillService.ensureMinHogaDepth(stockCode, "sys user");
//
//                    log.info("✅ {} 호가 업데이트 완료. 현재가 변경 감지됨: {}", stockCode, latestPrice);
//
//
//            } else {
//                    log.info("⏳ {} 호가 변경 없음. 현재가 동일: {}", stockCode, latestPrice);
//                }
//            } catch (Exception e) {
//                log.warn("❗ {} 호가 업데이트 실패: {}", stockCode, e.getMessage());
//            }
//        }
//    }
//}
