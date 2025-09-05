package com.example.iotl.domain.hoga;

import com.example.iotl.dto.hoga.HogaDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HogaGenerator {

    private static final int HOGA_DEPTH = 5;

    public static List<HogaDto> generate(int currentPrice) {
        int tickUnit = getTickUnit(currentPrice);
        List<HogaDto> hogas = new ArrayList<>();
        Random rand = new Random();

        // BUY
        for (int i = HOGA_DEPTH; i > 0; i--) {
            int price = currentPrice - i * tickUnit;
            int quantity = rand.nextInt(10) + 1;
            hogas.add(new HogaDto(price, quantity, HogaType.BUY));
        }

        // 현재가를 가운데로 삽입
        hogas.add(new HogaDto(currentPrice, rand.nextInt(10) + 1, HogaType.CURRENT));

        // SELL
        for (int i = 1; i <= HOGA_DEPTH; i++) {
            int price = currentPrice + i * tickUnit;
            int quantity = rand.nextInt(10) + 1;
            hogas.add(new HogaDto(price, quantity, HogaType.SELL));
        }

        return hogas;
    }

    public static List<HogaDto> update(List<HogaDto> existing) {
        Random rand = new Random();
        List<HogaDto> updated = new ArrayList<>();

        for (HogaDto dto : existing) {
            int change = rand.nextInt(3) - 1; // -1, 0, 1
            int newQty = Math.max(0, dto.getQuantity() + change);
            // 수량이 0이 아니면 추가
            if (newQty > 0) {
                updated.add(new HogaDto(dto.getPrice(), newQty, dto.getType()));
            }
        }

        return updated;
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
