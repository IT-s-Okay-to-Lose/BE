package com.example.iotl.service;


import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.TradeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderMatchingService {
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TradeService tradeService;

    /**
     * 무한 루프 방어용 상한.
     * 한 번의 주문이 체결할 수 있는 최대 상대 주문 건수.
     */
    private static final int MAX_MATCH_ITERATIONS = 100;

    /**
     * 신규 주문에 대해 반대 주문과의 매칭을 시도한다.
     *
     * <p>[공통 베이스 변경 - 동시성 대책과 직교]
     * 기존 구현은 후보 1건만 체결하고 {@code break} 했다. 그래서 신규 주문에
     * 잔량이 남아도 더 이상 체결하지 않고 PARTIAL 로 종료됐고, 그 PARTIAL 주문은
     * 조회 조건(status='PENDING')에서 빠져 영원히 재매칭되지 못했다.
     *
     * <p>이제 신규 주문의 잔량이 소진될 때까지 우선순위가 가장 높은 후보부터
     * 순차적으로 체결한다.
     *
     * <p>트랜잭션 경계: 이 메서드에는 @Transactional 을 붙이지 않는다.
     * 호출자인 OrderService.placeOrder() 가 이미 @Transactional 이고
     * TradeService.trade() 는 REQUIRED 이므로 셋이 하나의 물리 트랜잭션으로 묶인다.
     * (TransactionBoundaryProbeTest 로 실측 확인)
     */
    public void match(Order newOrder) {
        OrderType oppositeType = newOrder.getOrderType() == OrderType.BUY
            ? OrderType.SELL
            : OrderType.BUY;

        int iterations = 0;
        int consumedRetries = 0;
        while (newOrder.getQuantity() > 0 && iterations++ < MAX_MATCH_ITERATIONS) {
            Order candidate = nextCandidate(newOrder, oppositeType);
            if (candidate == null) {
                break;  // 더 이상 체결할 상대가 없다
            }

            int before = newOrder.getQuantity();
            TradeResult result = tradeService.trade(newOrder, candidate);

            if (result.isBusinessFailure()) {
                // 자산 부족은 재시도해도 해결되지 않는다. 매칭을 중단한다.
                break;
            }
            if (result.isRetriable()) {
                // [B2-1] 정상 경합: 다른 트랜잭션이 후보를 먼저 소비했다.
                // 예외가 아니므로 rollback-only 가 되지 않고, 다음 후보로 진행할 수 있다.
                // [B2-2] READ_COMMITTED 이므로 재조회가 최신 커밋을 본다.
                consumedRetries++;
                if (consumedRetries > MAX_MATCH_ITERATIONS) {
                    break;
                }
                continue;
            }

            // 방어: 체결이 잔량을 줄이지 못했다면 무한 루프가 되므로 중단한다.
            if (newOrder.getQuantity() >= before) {
                break;
            }
        }

        // 잔량이 남았는데 아직 PENDING 이면 상태를 정리한다.
        if (newOrder.getQuantity() > 0 && newOrder.getStatus() == OrderStatus.PENDING) {
            orderRepository.save(newOrder);
        }
    }

    /**
     * 우선순위가 가장 높은 후보 1건을 반환한다. 없으면 null.
     *
     * <p>이 메서드를 후보 A/B 브랜치에서 각각 다르게 구현한다.
     * 공통 베이스에서는 락 없이 조회한다(= 현재의 동시성 결함이 그대로 남음).
     */
    protected Order nextCandidate(Order newOrder, OrderType oppositeType) {
        List<Order> candidates = orderRepository.findMatchingOrders(
            oppositeType,
            newOrder.getStock().getStockCode(),
            newOrder.getPrice(),
            newOrder.getId(),
            newOrder.getUser().getUserId());
        return candidates.isEmpty() ? null : candidates.get(0);
    }
}
