package com.example.iotl.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.iotl.dto.security.OAuth2Response;
import com.example.iotl.dto.UserDto;
import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.User;
import com.example.iotl.repository.UserRepository;
import com.example.iotl.repository.AccountsRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Test
    @DisplayName("OAuth2 신규 유저 생성 시 계좌도 함께 생성되고 초기 잔액은 100만원이어야 한다")
    void createUserWithAccount() {
        // given
        OAuth2Response dummyOAuthUser = new OAuth2Response() {
            @Override public String getProvider() { return "kakao"; }
            @Override public String getProviderId() { return "123456"; }
            @Override public String getEmail() { return "testuser@exㄷample.com"; }
            @Override public String getName() { return "테스트유저"; }

            @Override
            public String getProfileImage() {
                return "";
            }
        };

        // when
        UserDto result = userService.saveOrUpdateOAuthUser(dummyOAuthUser);

        // then
        User user = userRepository.findByUsername(result.getUsername());
        assertNotNull(user, "User 저장 확인");

        Accounts account = user.getAccount();
        assertNotNull(account, "Accounts 연관 객체 확인");
        assertEquals(new BigDecimal("1000000.00"), account.getBalance(), "초기 예수금 100만원 확인");

        // 보너스: 저장된 계좌가 DB에도 존재하는지 확인
        Accounts byDb = accountsRepository.findById(account.getAccountId()).orElse(null);
        assertNotNull(byDb, "DB에 실제 계좌가 저장되었는지 확인");
    }
}
