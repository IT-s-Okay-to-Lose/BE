package com.example.iotl.service;

import com.example.iotl.dto.deposit.DepositRequest;
import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountsRepository accountsRepository;
    private final UserRepository userRepository;

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
}
