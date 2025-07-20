package com.example.iotl.service;

import com.example.iotl.dto.holding.HoldingSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class HoldingServiceImplTest {

    @Autowired
    private HoldingServiceImpl holdingService;

    @Test
    void getHoldingsByUserName_success() {
        // ✅ 실제 존재하는 username으로 테스트
        String username = "kakao 4313039876";

        List<HoldingSummaryDto> holdings = holdingService.getHoldingsByUserName(username);

        assertNotNull(holdings, "결과 리스트는 null이 아니어야 합니다.");

        if (holdings.isEmpty()) {
            System.out.println("📭 조회된 holdings 없음");
        } else {
            holdings.forEach(dto -> {
                System.out.println("📌 종목명: " + dto.getStockName());
                System.out.println("   이미지 url : " + dto.getStockImageUrl());
                System.out.println("   보유 수량: " + dto.getQuantity());
                System.out.println("   평균 단가: " + dto.getAvgBuyPrice());
                System.out.println("   등락률: " + dto.getChangeRate() + "%");
                System.out.println("----------------------------------------");
            });
        }
    }

    @Test
    void getHoldingsByUserName_invalidUser() {
        String invalidUsername = "존재하지않는유저";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            holdingService.getHoldingsByUserName(invalidUsername);
        });

        assertTrue(exception.getMessage().contains("해당 username을 가진 유저가 없습니다"));
    }
}