//package com.example.iotl.service;
//
//import com.example.iotl.domain.hoga.HogaGenerator;
//import com.example.iotl.domain.hoga.HogaType;
//import com.example.iotl.dto.hoga.HogaDto;
//import com.example.iotl.dto.order.OrderRequestDto;
//import com.example.iotl.entity.Order;
//import com.example.iotl.entity.Stocks;
//import com.example.iotl.repository.StocksRepository;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class OrderGeneratorService {
//
//    private final OrderService orderService;
//    private final StocksRepository stocksRepository;
//
//    public List<HogaDto> generateOrdersAroundPrice(String stockCode, int currentPrice, String username) {
//        Stocks stock = stocksRepository.findById(stockCode)
//            .orElseThrow(() -> new IllegalArgumentException("종목 없음: " + stockCode));
//
//        int tick = HogaGenerator.getTickUnit(currentPrice);
//        Random rand = new Random();
//        List<HogaDto> generated = new ArrayList<>();
//
//        // BUY 주문: 현재가 아래로 5개
//        for (int i = 5; i >= 1; i--) {
//            int price = currentPrice - i * tick;
//            int quantity = rand.nextInt(10) + 1;
//
//            OrderRequestDto buyDto = OrderRequestDto.builder()
//                .stockCode(stockCode)
//                .orderType(Order.OrderType.BUY)
//                .price(BigDecimal.valueOf(price))
//                .quantity(quantity)
//                .build();
//
//            orderService.placeOrder(username, buyDto);
//            generated.add(new HogaDto(price, quantity, HogaType.BUY));
//        }
//
//        // 현재가 SELL (3~10개 랜덤)
//        int currentQty = rand.nextInt(8) + 3;
//        orderService.placeOrder(username,
//            OrderRequestDto.builder()
//                .stockCode(stockCode)
//                .orderType(Order.OrderType.SELL)
//                .price(BigDecimal.valueOf(currentPrice))
//                .quantity(currentQty)
//                .build());
//        generated.add(new HogaDto(currentPrice, currentQty, HogaType.SELL));
//
//        // SELL 주문: 현재가 위로 5개
//        for (int i = 1; i <= 5; i++) {
//            int price = currentPrice + i * tick;
//            int quantity = rand.nextInt(10) + 1;
//
//            OrderRequestDto sellDto = OrderRequestDto.builder()
//                .stockCode(stockCode)
//                .orderType(Order.OrderType.SELL)
//                .price(BigDecimal.valueOf(price))
//                .quantity(quantity)
//                .build();
//
//            orderService.placeOrder(username, sellDto);
//            generated.add(new HogaDto(price, quantity, HogaType.SELL));
//        }
//
//        return generated;
//    }
//
//}
