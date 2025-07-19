//package com.example.iotl.init;
//
//import com.example.iotl.dto.hoga.HogaDto;
//import com.example.iotl.repository.StocksRepository;
//import com.example.iotl.service.HogaRedisService;
//import com.example.iotl.service.HogaService;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class HogaInitializer implements ApplicationRunner {
//
//    private final StocksRepository stockInfoRepository;
//    private final HogaService hogaService;
//    private final HogaRedisService hogaRedisService;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        List<String> stockCodes = stockInfoRepository.findAllStockCodes();
//
//        for (String stockCode : stockCodes) {
//            try {
//                List<HogaDto> hogas = hogaService.getHogas(stockCode);
//                hogaRedisService.saveHoga(stockCode, hogas);
//                log.info("✅ 앱 시작 시 호가 초기화 완료 - {}", stockCode);
//            } catch (Exception e) {
//                log.warn("❗ 호가 초기화 실패 - {}: {}", stockCode, e.getMessage());
//            }
//        }
//    }
//}
