//package com.example.iotl.service;
//
//import com.example.iotl.domain.hoga.HogaType;
//import com.example.iotl.dto.hoga.HogaDto;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class HogaAutoFillService {
//
//    private final HogaRedisService hogaRedisService;
//    private final HogaService hogaService;
//    private final OrderGeneratorService orderGeneratorService;
//
//    public void ensureMinHogaDepth(String stockCode, String username) {
//        int currentPrice = hogaService.getLatestClosePrice(stockCode).intValue();
//        List<HogaDto> hogas = hogaRedisService.getHoga(stockCode);
//
//        boolean hasBuy = hogas.stream()
//            .anyMatch(h -> h.getType() == HogaType.BUY && h.getPrice() < currentPrice);
//
//        boolean hasSell = hogas.stream()
//            .anyMatch(h -> h.getType() == HogaType.SELL && h.getPrice() >= currentPrice); // 현재가 포함
//
//        if (!hasBuy || !hasSell) {
//            System.out.println("🔧 호가 부족 → 주문 생성");
//            orderGeneratorService.generateOrdersAroundPrice(stockCode, currentPrice, username);
//        } else {
//            System.out.println("✅ 호가 충분: 추가 주문 생략");
//        }
//    }
//}
