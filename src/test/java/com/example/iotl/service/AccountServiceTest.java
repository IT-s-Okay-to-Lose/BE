package com.example.iotl.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;


    @ExtendWith(SpringExtension.class)
    @SpringBootTest
    @ActiveProfiles("test")
    @Transactional
    public class AccountServiceTest {

        @Autowired
        private AccountService accountService;
        @Autowired
        private AccountsRepository accountsRepository;

        @Autowired
        private UserRepository userRepository;

        @Test
        void 입금_성공_테스트() {
            // given: 테스트용 사용자와 계좌 저장
            User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .role("USER") // ← 필수!
                .build();

            userRepository.save(user);

            Accounts account = Accounts.builder()
                .user(user)
                .balance(BigDecimal.valueOf(10000))
                .build();
            // 반드시 user와 account가 서로 연결되게!
            account.setUser(user); // 양방향 매핑 설정
            accountsRepository.save(account);

            // when
            accountService.deposit("testuser", BigDecimal.valueOf(5000));

            // then
            Accounts updated = accountsRepository.findById(account.getAccountId()).orElseThrow();
            assertEquals(BigDecimal.valueOf(15000), updated.getBalance());
        }

    }

