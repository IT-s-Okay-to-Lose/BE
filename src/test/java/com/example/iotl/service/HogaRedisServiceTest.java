package com.example.iotl.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.iotl.domain.hoga.HogaGenerator;
import com.example.iotl.domain.hoga.HogaType;
import com.example.iotl.dto.hoga.HogaDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HogaRedisServiceTest {

    @Autowired
    HogaRedisService hogaRedisService;

//    @Test
//    void saveHoga() {
//    }
//
//    @Test
//    void getHoga() {
//    }
//
//    @Test
//    void saveCurrentPrice() {
//    }
//
//    @Test
//    void getLastPrice() {
//    }

    @Test
    @DisplayName(" 랜덤 수량 업데이트가 정상 작동하며 리스트 크기를 유지해야 한다")
    void testUpdateQuantityRandomly() {
        // given
        String stockCode = "A001";
        List<HogaDto> originalHoga = HogaGenerator.generate(60000);
        hogaRedisService.saveHoga(stockCode, originalHoga);

        // when
        hogaRedisService.updateQuantitiesRandomly(stockCode);
        List<HogaDto> updatedHoga = hogaRedisService.getHoga(stockCode);

        // then
        assertThat(updatedHoga).isNotEmpty();
        assertThat(updatedHoga).hasSize(originalHoga.size());
    }

    @Test
    @DisplayName(" 주문 체결 시 해당 호가 수량이 감소해야 한다")
    void testDecreaseQuantityOnMatch() {
        // given
        String stockCode = "A001";
        int price = 60000;
        List<HogaDto> hogas = HogaGenerator.generate(price);
        hogas.get(0).setPrice(price); // 확실히 매칭되게 가격 세팅
        hogas.get(0).setType(HogaType.BUY);
        hogas.get(0).setQuantity(10);
        hogaRedisService.saveHoga(stockCode, hogas);

        // when
        hogaRedisService.decreaseQuantityOnMatch(stockCode, price, HogaType.BUY, 5);

        // then
        List<HogaDto> result = hogaRedisService.getHoga(stockCode);
        assertThat(result.get(0).getQuantity()).isEqualTo(5);
    }
}
