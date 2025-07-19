package com.example.iotl.service;

import com.example.iotl.domain.hoga.HogaType;
import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.entity.Order;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HogaRedisService {

    private static final String HOGA_KEY_PREFIX = "hoga:";
    private static final String PRICE_KEY_PREFIX = "lastPrice:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveHoga(String stockCode, List<HogaDto> hogaList) {
        redisTemplate.opsForValue().set(HOGA_KEY_PREFIX + stockCode, hogaList);
    }

    @SuppressWarnings("unchecked")
    public List<HogaDto> getHoga(String stockCode) {
        Object data = redisTemplate.opsForValue().get(HOGA_KEY_PREFIX + stockCode);
        if (data instanceof List<?>) {
            List<?> list = (List<?>) data;
            List<HogaDto> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof LinkedHashMap) {
                    LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) item;

                    int price = (int) map.get("price");
                    int quantity = (int) map.get("quantity");
                    HogaType type = HogaType.valueOf((String) map.get("type"));

                    result.add(new HogaDto(price, quantity, type));
                }
            }
            return result;
        }
        return List.of();
    }


    public void saveCurrentPrice(String stockCode, BigDecimal price) {
        redisTemplate.opsForValue().set(PRICE_KEY_PREFIX + stockCode, price);
    }

    public Optional<BigDecimal> getLastPrice(String stockCode) {
        Object data = redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + stockCode);
        if (data instanceof BigDecimal) {
            return Optional.of((BigDecimal) data);
        }
        return Optional.empty();
    }

    // 수량만 랜덤하게 업데이트 (현재가 변화 없이)
//    public void updateQuantitiesRandomly(String stockCode) {
//        List<HogaDto> hogaList = getHoga(stockCode);
//        Random random = new Random();
//        for (HogaDto hoga : hogaList) {
//            int delta = random.nextInt(5) - 2; // -2 ~ +2
//            int newQty = Math.max(1, hoga.getQuantity() + delta); // 최소 수량 1
//            hoga.setQuantity(newQty);
//        }
//        saveHoga(stockCode, hogaList);
//    }

    // 주문 체결 시 호가 수량 감소
    public void decreaseQuantityOnMatch(String stockCode, int price, HogaType type, int matchedQty) {
        List<HogaDto> hogaList = getHoga(stockCode);
        for (HogaDto hoga : hogaList) {
            if (hoga.getPrice() == price && hoga.getType() == type) {
                int newQty = Math.max(0, hoga.getQuantity() - matchedQty);
                hoga.setQuantity(newQty);
                break;
            }
        }
        saveHoga(stockCode, hogaList);
    }

//    public void updateHogaQuantity(String stockCode, Order.OrderType type, int price, int quantityToSubtract) {
//        String key = HOGA_KEY_PREFIX + stockCode;
//        Object data = redisTemplate.opsForValue().get(key);
//
//        if (data instanceof List<?>) {
//            List<?> list = (List<?>) data;
//            List<HogaDto> updated = new ArrayList<>();
//
//            for (Object obj : list) {
//                if (obj instanceof HogaDto hoga) {
//                    // 타입과 가격이 일치하는 경우 수량 차감
//                    if (hoga.getType() == type && hoga.getPrice() == price) {
//                        int newQuantity = Math.max(0, hoga.getQuantity() - quantityToSubtract);
//                        hoga.setQuantity(newQuantity);
//                    }
//                    updated.add(hoga);
//                }
//            }
//
//            redisTemplate.opsForValue().set(key, updated);
//        }
//    }





}
