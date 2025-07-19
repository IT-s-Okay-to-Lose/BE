package com.example.iotl.service;

import com.example.iotl.domain.hoga.HogaType;
import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StockDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HogaService {

    private final OrderRepository orderRepository;
    private final StockDetailRepository stockDetailRepository;


    public List<HogaDto> getHogas(String stockCode) {
        List<HogaDto> buyHogas = aggregateOrders(stockCode, OrderType.BUY);
        List<HogaDto> sellHogas = aggregateOrders(stockCode, OrderType.SELL);

        // BUY는 내림차순(높은 가격 우선), SELL은 오름차순(낮은 가격 우선)
        buyHogas.sort(Comparator.comparing(HogaDto::getPrice).reversed());
        sellHogas.sort(Comparator.comparing(HogaDto::getPrice));

        buyHogas.forEach(h -> h.setType(HogaType.BUY));
        sellHogas.forEach(h -> h.setType(HogaType.SELL));

        buyHogas.addAll(sellHogas);
        return buyHogas;
    }

    private List<HogaDto> aggregateOrders(String stockCode, OrderType type) {
        return orderRepository.findByStock_StockCodeAndOrderTypeAndStatus(
                stockCode, type, OrderStatus.PENDING
            ).stream()
            .collect(Collectors.groupingBy(Order::getPrice, Collectors.summingInt(Order::getQuantity)))
            .entrySet().stream()
            .map(e -> new HogaDto(e.getKey().intValue(), e.getValue(), null))
            .collect(Collectors.toList());
    }

    public BigDecimal getLatestClosePrice(String stockCode) {
        return stockDetailRepository.findTopByStocks_StockCodeOrderByCreatedAtDesc(stockCode)
            .map(sd -> sd.getClosePrice())
            .orElseThrow(() -> new IllegalArgumentException("현재가 데이터가 없습니다: " + stockCode));
    }
}
