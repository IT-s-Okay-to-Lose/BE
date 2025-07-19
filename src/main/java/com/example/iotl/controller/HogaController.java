package com.example.iotl.controller;

import com.example.iotl.domain.hoga.HogaGenerator;
import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.service.HogaAutoFillService;
import com.example.iotl.service.HogaRedisService;
import com.example.iotl.service.HogaService;
import com.example.iotl.service.OrderGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/api/hoga")
public class HogaController {

    private final HogaService hogaService;
    private final HogaRedisService hogaRedisService;
    private final HogaAutoFillService hogaAutoFillService;
    private final StocksRepository stocksRepository;
    private final OrderGeneratorService orderGeneratorService;


//    @GetMapping
//    public ResponseEntity<List<HogaDto>> getHogas(@RequestParam("code") String stockCode) {
//        List<HogaDto> hogas = hogaService.getHogas(stockCode);
//        return ResponseEntity.ok(hogas);
//    }


    @PostMapping("/{stockCode}")
    public void saveHoga(@PathVariable String stockCode) {
        List<HogaDto> hogas = hogaService.getHogas(stockCode); // 현재가 기반
        hogaRedisService.saveHoga(stockCode, hogas);
    }

    @GetMapping("/{stockCode}")
    public List<HogaDto> getHoga(@PathVariable String stockCode) {
        return hogaRedisService.getHoga(stockCode);
    }

    @GetMapping("/fill")
    public String testHogaFill(@RequestParam String stockCode) {
        hogaAutoFillService.ensureMinHogaDepth(stockCode, "system_user@iotl.com");
        return "호가 확인 및 부족 시 자동 주문 완료";
    }

    @PostMapping("/bulk-init")
    public String initAllHogas() {
        List<String> stockCodes = stocksRepository.findAllStockCodes();

        for (String stockCode : stockCodes) {
            int currentPrice = hogaService.getLatestClosePrice(stockCode).intValue();

            // 주문 생성 + HogaDto 리스트 반환
            List<HogaDto> generatedHogas =
                orderGeneratorService.generateOrdersAroundPrice(stockCode, currentPrice, "sys user");

            // 바로 Redis 저장
            hogaRedisService.saveHoga(stockCode, generatedHogas);
        }

        return "✅ 모든 종목 호가 초기화 완료 (주문 + Redis)!";
    }








}
