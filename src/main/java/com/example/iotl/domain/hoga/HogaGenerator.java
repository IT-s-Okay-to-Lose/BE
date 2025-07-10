package com.example.iotl.domain.hoga;

import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.domain.hoga.HogaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HogaGenerator {

    private static final int HOGA_DEPTH = 5;

    public static List<HogaDto> generate(int currentPrice) {
        int tickUnit = getTickUnit(currentPrice);
        List<HogaDto> hogas = new ArrayList<>();
        Random rand = new Random();

        // BUY (매수 호가)
        for (int i = HOGA_DEPTH; i > 0; i--) {
            int price = currentPrice - i * tickUnit;
            int quantity = rand.nextInt(10) + 1;

            hogas.add(HogaDto.builder()
                .price(price)
                .quantity(quantity)
                .type(HogaType.BUY)
                .build());
        }

        // SELL (매도 호가)
        for (int i = 1; i <= HOGA_DEPTH; i++) {
            int price = currentPrice + i * tickUnit;
            int quantity = rand.nextInt(10) + 1;

            hogas.add(HogaDto.builder()
                .price(price)
                .quantity(quantity)
                .type(HogaType.SELL)
                .build());
        }

        return hogas;
    }

    public static int getTickUnit(int price) {
        if (price < 1000) return 1;
        else if (price < 2000) return 1;
        else if (price < 5000) return 5;
        else if (price < 10000) return 10;
        else if (price < 20000) return 10;
        else if (price < 50000) return 50;
        else if (price < 100000) return 100;
        else if (price < 200000) return 100;
        else if (price < 500000) return 500;
        else return 1000;
    }
}
