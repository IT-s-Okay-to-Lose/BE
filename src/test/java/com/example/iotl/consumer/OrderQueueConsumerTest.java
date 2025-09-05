package com.example.iotl.consumer;

import static org.mockito.Mockito.*;

import com.example.iotl.entity.Order;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.service.OrderMatchingService;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ListOperations;

class OrderQueueConsumerTest {



    @Test
    @DisplayName("Redis 큐에서 주문을 꺼내고 체결까지 시도한다")
    void testConsumeOrders() {
        // given
        String stockCode = "A001";
        Long orderId = 123L;

        // RedisTemplate mock
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ListOperations<String, Object> listOps = mock(ListOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPop("order:queue:" + stockCode)).thenReturn(orderId);

        // OrderRepository mock
        OrderRepository orderRepository = mock(OrderRepository.class);
        Order order = mock(Order.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // OrderMatchingService mock
        OrderMatchingService orderMatchingService = mock(OrderMatchingService.class);

        // StockInfoRepository mock
        StockInfoRepository stockInfoRepository = mock(StockInfoRepository.class);
        when(stockInfoRepository.findAllStockCodes()).thenReturn(List.of(stockCode));

        // 클래스 생성
        OrderQueueConsumer consumer = new OrderQueueConsumer(
            redisTemplate, orderRepository, orderMatchingService, stockInfoRepository
        );

        // when
        consumer.consumeOrders();

        // then
        verify(orderMatchingService, times(1)).match(order);
    }
}
