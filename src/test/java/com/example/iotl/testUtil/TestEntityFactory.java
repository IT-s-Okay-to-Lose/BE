package com.example.iotl.testUtil;

import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;

public class TestEntityFactory {

    public static User createUser(String name) {
        return User.builder()
            .username("SELLER" + System.nanoTime()) // OAuth2 기준 username
            .name("seller")
            .role("USER")
            .profileImage("blahblah")
            .email("seller" + System.currentTimeMillis() + "@test.com")
            .build();
    }

    public static Stocks createStock(String name) {
        return Stocks.builder()
            .stockName(name)
            .stockCode("005930")  // 삼성전자 예시
            .build();
    }
}
