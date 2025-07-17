package com.example.iotl.service;

import com.example.iotl.dto.holding.HoldingSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class HoldingServiceImplTest {

    @Autowired
    private HoldingServiceImpl holdingService;

    @Test
    void getHoldingsByUserName() {
        String name = "이혜원";

        List<HoldingSummaryDto> holdings = holdingService.getHoldingsByUserName(name);

        if (holdings.isEmpty()) {
            System.out.println("📭 조회된 holdings 없음");
        } else {
            holdings.forEach(dto -> {
                System.out.println("📌 종목명: " + dto.getStockName());
                System.out.println("   보유 수량: " + dto.getQuantity());
                System.out.println("   평균 단가: " + dto.getAvgBuyPrice());
                System.out.println("   등락률: " + dto.getChangeRate() + "%");
                System.out.println("----------------------------------------");
            });
        }
    }
}