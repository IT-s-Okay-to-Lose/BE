package com.example.iotl.service;


import com.example.iotl.dto.OrderHistoryDto;
import com.example.iotl.dto.order.OrderRequestDto;
import com.example.iotl.dto.order.OrderResponseDto;
import com.example.iotl.entity.Holdings;

import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.repository.UserRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final jakarta.persistence.EntityManager entityManager;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StocksRepository stocksRepository;
    private final HoldingsRepository holdingsRepository;
    private final TradeService tradeService;
    private final OrderMatchingService orderMatchingService;

    /**
     * [B2-2] 이 트랜잭션에만 READ_COMMITTED 를 적용한다.
     *
     * <p>왜 이 트랜잭션에서 READ COMMITTED 가 필요한가:
     * 매칭은 "후보를 읽고 → CAS 로 소비를 시도하고 → 실패하면 다음 후보를 다시 읽는"
     * 반복 구조다. REPEATABLE READ 에서는 일반 SELECT 가 트랜잭션 첫 읽기 시점의
     * 스냅샷을 계속 사용하므로, 재조회를 해도 <b>이미 소비된 후보가 그대로 다시 나온다</b>
     * (RepeatableReadSnapshotProbeTest 로 실측). 매 SELECT 마다 새 스냅샷이 필요하다.
     *
     * <p>MySQL 전역 격리수준은 바꾸지 않는다. 이 경로에만 적용한다.
     *
     * <p>부작용 검토(B2-3): 이 경로의 조회 중 스냅샷 일관성을 필요로 하는 것은 없다.
     * Holdings/Accounts 체결 조회는 PESSIMISTIC_WRITE 잠금 읽기라 격리수준과 무관하게
     * 항상 최신 커밋을 읽고, User/Stocks 는 트랜잭션 중 1회만 읽는 마스터성 데이터다.
     *
     * <p>애노테이션을 jakarta -> Spring 으로 교체한 이유: jakarta.transaction.Transactional
     * 에는 isolation 속성이 없다. TransactionTemplate 를 쓰면 메서드 전체를 콜백으로
     * 감싸야 해 기존 구조 변경이 크므로, 선언적 애노테이션 교체를 택했다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderResponseDto placeOrder(String username, OrderRequestDto requestDto) {

        System.out.println("🟢 orderType: " + requestDto.getOrderType());
        System.out.println("🟢 stockCode: " + requestDto.getStockCode());

        // (1) 유저 찾기

        // [수정] User 엔티티를 로드하지 않고 식별자만 조회한 뒤 프록시 참조를 쓴다.
        //
        // findByUsername 으로 User 를 로드하면 User.account 가 함께 로드된다.
        // mappedBy 인 @OneToOne 은 연관이 null 인지 알아야 하므로 provider 가
        // fetch=LAZY 선언을 무시하고 조회한다(LAZY_FETCH_PROBE 실측: isLoaded=true).
        //
        // 그러면 Accounts 가 managed 로 올라가고, 체결 단계의
        // accountsRepository.findByUserWithPessimisticLock 이 1차 캐시의 낡은
        // balance 를 반환해 lost update 가 발생한다
        // (managed=1,000,000 / DB=995,000 / 잠금조회 후=1,000,000).
        //
        // 식별자만 조회하면 User 도 Accounts 도 로드되지 않으므로,
        // 체결 단계의 잠금 조회가 Accounts 의 첫 load 가 되어 DB 최신값을 읽는다.
        // getReference 는 SELECT 를 발생시키지 않는 프록시이며,
        // Order 의 연관관계 설정에는 식별자만 있으면 충분하다.
        Long userId = userRepository.findUserIdByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        User user = entityManager.getReference(User.class, userId);

        // (2) 종목 찾기
        Stocks stocks = stocksRepository.findById(requestDto.getStockCode())
            .orElseThrow(() -> new IllegalArgumentException("해당 주식이 존재하지 않습니다."));


        // ======== 매도 주문일 때 보유 수량 체크 =========
        if (requestDto.getOrderType() == Order.OrderType.SELL) {
            // [수정안 D] 엔티티가 아니라 quantity 스칼라만 조회한다.
            //
            // 엔티티로 조회하면 Holdings 가 영속성 컨텍스트에 managed 상태로 올라가고,
            // 같은 트랜잭션의 체결 단계에서 PESSIMISTIC_WRITE 로 재조회해도
            // 1차 캐시의 낡은 값이 반환된다(PersistenceContextProbeTest 실측:
            // managed=100 / DB=93 / 잠금조회 후=100). 그 값으로 갱신하면
            // 다른 트랜잭션의 커밋이 덮어써진다(lost update).
            //
            // 스칼라 조회는 엔티티를 로드하지 않으므로, 체결 단계의 잠금 조회가
            // Holdings 엔티티의 첫 load 가 되어 DB 최신값을 읽는다.
            //
            // 이 검증은 빠른 실패를 위한 advisory validation 이다.
            // 실제 자산 변경 판단은 체결 단계의 잠금 조회가 authoritative 하다.
            int currentQuantity = holdingsRepository
                .findQuantityByUserAndStock(user, stocks)
                .orElseThrow(() -> new IllegalStateException("해당 종목을 보유하고 있지 않습니다."));
            if (currentQuantity < requestDto.getQuantity()) {
                throw new IllegalStateException("보유 수량보다 많은 매도 주문은 불가합니다.");
            }
        }



        // (3) Order 객체 생성
        Order order = Order.builder()
            .user(user)
            .stock(stocks)
            .orderType(requestDto.getOrderType())
            .price(requestDto.getPrice())
            .quantity(requestDto.getQuantity())
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // (4) 저장
        orderRepository.save(order);

        //  (5) 체결 시도
        orderMatchingService.match(order);

        return OrderResponseDto.from(order);
    }


    public List<OrderHistoryDto> getOrderHistoryByUserAndStock(User user, String stockCode) {
        List<Order> orders = orderRepository.findByUserAndStock_StockCode(user, stockCode);
        return orders.stream()
            .map(OrderHistoryDto::from)
            .collect(Collectors.toList());
    }


    public List<OrderHistoryDto> getOrderHistoryByUsernameAndStock(String username, String stockCode) {
        User user = Optional.ofNullable(userRepository.findByUsername(username))
            .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findAllByUserAndStockStockCode(user, stockCode)
            .stream()
            .map(OrderHistoryDto::from)
            .toList();
    }

//
//    private void handleBuyOrder(User user, Stocks stock, OrderRequestDto dto) {
//        // 가격과 수량 유효성 검사
//        if (dto.getPrice().compareTo(BigDecimal.ZERO) <= 0 || dto.getQuantity() <= 0) {
//            throw new IllegalArgumentException("가격과 수량은 0보다 커야 합니다.");
//        }
//        // 총 주문 금액 = 가격 × 수량
//        BigDecimal totalCost = dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
//        BigDecimal currentBalance = user.getAccount().getBalance();
//
//        if (currentBalance.compareTo(totalCost) < 0) {
//            throw new IllegalStateException("예수금이 부족합니다.");
//        }
//
//        BigDecimal updatedBalance = currentBalance.subtract(totalCost);
//        user.getAccount().setBalance(updatedBalance.setScale(2, RoundingMode.HALF_UP));
//    }
//
//
//    private void handleSellOrder(User user, Stocks stock, OrderRequestDto dto) {
//        // (1) 보유 종목 조회
//
//        Holdings holding = holdingsRepository.findByUserAndStock(user, stock)
//            .orElseThrow(() -> new IllegalArgumentException("해당 종목을 보유하고 있지 않습니다."));
//
//        // (2) 보유 수량 >= 매도 수량 확인
//        if (holding.getQuantity() < dto.getQuantity()) {
//            throw new IllegalStateException("보유 수량이 부족합니다.");
//        }
//
//        // (3) 보유 수량 차감
//        holding.setQuantity(holding.getQuantity() - dto.getQuantity());
//
//        // (4) 매도 금액 계산 후 예수금 증가
//        BigDecimal sellAmount = dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
//        BigDecimal updatedBalance = user.getAccount().getBalance().add(sellAmount);
//
//        user.getAccount().setBalance(updatedBalance.setScale(2, RoundingMode.HALF_UP));
//    }
//


}
