package com.example.iotl.service;

import com.example.iotl.dto.AccountSummaryDto;
import com.example.iotl.dto.deposit.DepositRequest;
import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.StockDetailRepository;
import com.example.iotl.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountsRepository accountsRepository;
    private final UserRepository userRepository;
    private final HoldingsRepository holdingsRepository;
    private final StockDetailRepository stockDetailRepository;


    @Transactional
    public void deposit(String username, BigDecimal amount) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Accounts account = user.getAccount();
        if (account == null) {
            throw new IllegalStateException("Account not found");
        }

        account.setBalance(account.getBalance().add(amount));
    }

    public AccountSummaryDto getAccountSummary(String username) {
        Accounts account = accountsRepository.findByUser_Username(username)
            .orElseThrow(() -> new RuntimeException("계좌 없음"));

        BigDecimal balance = account.getBalance();

        List<Holdings> holdings = holdingsRepository.findByUser_Username(username);

        BigDecimal investingAmount = holdings.stream()
            .map(h -> {
                BigDecimal currentPrice = stockDetailRepository
                    .findTopByStocks_StockCodeOrderByCreatedAtDesc(h.getStock().getStockCode())
                    .map(StockDetail::getClosePrice)
                    .orElse(BigDecimal.ZERO);
                return currentPrice.multiply(BigDecimal.valueOf(h.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AccountSummaryDto(balance, investingAmount);
    }


}
