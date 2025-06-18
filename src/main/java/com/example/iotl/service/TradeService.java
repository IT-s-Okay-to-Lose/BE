package com.example.iotl.service;

import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StockRepository;
import com.example.iotl.repository.TradeRepository;
import com.example.iotl.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final UserRepository userRepository;
    private final AccountsRepository accountRepository;
    private final StockRepository stockRepository;
    private final HoldingsRepository holdingsRepository;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public void buyStock(Long userId, String stockCode, int quantity, BigDecimal price) {
        // TODO: 매수 로직 구현
    }

}
