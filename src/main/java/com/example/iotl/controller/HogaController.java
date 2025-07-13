package com.example.iotl.controller;

import com.example.iotl.domain.hoga.HogaGenerator;
import com.example.iotl.dto.hoga.HogaDto;
import com.example.iotl.service.HogaRedisService;
import com.example.iotl.service.HogaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hoga")
public class HogaController {

    private final HogaService hogaService;
    private final HogaRedisService hogaRedisService;

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



}
