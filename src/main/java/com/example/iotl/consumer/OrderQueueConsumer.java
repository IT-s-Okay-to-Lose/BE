package com.example.iotl.consumer;

import com.example.iotl.entity.Order;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.service.OrderMatchingService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderQueueConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderRepository orderRepository;
    private final OrderMatchingService orderMatchingService;
    private final StocksRepository stockInfoRepository;

    // 매 1초마다 Redis 큐에서 주문 ID 꺼내서 처리
    @Scheduled(fixedDelay = 1000)
    public void consumeOrders() {
        for (String stockCode : getTrackedStockCodes()) {
            String queueKey = "order:queue:" + stockCode;

            Object orderIdObj = redisTemplate.opsForList().rightPop(queueKey); // FIFO
            if (orderIdObj == null) continue;

            try {
                Long orderId = Long.valueOf(orderIdObj.toString());
                Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문 ID 찾을 수 없음: " + orderId));

                orderMatchingService.match(order);
                log.info("✅ 주문 처리 완료 (주문 ID: {})", orderId);
            } catch (Exception e) {
                log.error("❗ 주문 처리 실패: {}", e.getMessage());
            }
        }
    }

    private String[] getTrackedStockCodes() {
        List<String> codes = stockInfoRepository.findAllStockCodes();
        return codes.toArray(new String[0]);
    }
}
