package com.example.iotl.controller;

import com.example.iotl.dto.OrderHistoryDto;
import com.example.iotl.dto.TradeDto;
import com.example.iotl.entity.User;
import com.example.iotl.repository.UserRepository;
import com.example.iotl.service.TradeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {
    private final UserRepository userRepository;
    private final TradeService tradeService;

    @GetMapping
    public ResponseEntity<List<TradeDto>> getTrades(
        @RequestParam Long userId,
        @RequestParam String stockCode) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(tradeService.getTradesByUserAndStock(user, stockCode));
    }

}
