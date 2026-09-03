package com.example.iotl.repository;

import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.User;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. 주문 매칭 (체결용)
    //
    // [공통 베이스 변경 - 동시성 대책과 직교]
    //  (1) status 를 PENDING + PARTIAL 로 확장한다.
    //      기존에는 PENDING 만 조회해서, 부분 체결로 PARTIAL 이 된 주문이
    //      이후 어떤 신규 주문과도 매칭되지 못하고 영구히 대기했다.
    //      (PartialOrderRematchTest 로 재현됨)
    //  (2) 정렬에 orderId 를 tie-breaker 로 추가한다.
    //      created_at 은 초 단위 해상도라 동시 생성된 주문의 순서가 비결정적이었다.
    //      가격·시간 우선 원칙을 결정론적으로 만들기 위함이다.
    //  (3) 자기 자신(newOrder)과 자기 주문끼리의 매칭을 제외한다.
    //      매칭 loop 도입 시 자기 주문을 후보로 집어올 수 있기 때문이다.
    @Query("SELECT o FROM Order o " +
        "WHERE o.status IN ('PENDING', 'PARTIAL') " +
        "AND o.orderType = :orderType " +
        "AND o.stock.stockCode = :stockCode " +
        "AND o.price = :price " +
        "AND o.id <> :excludeOrderId " +
        "AND o.user.userId <> :excludeUserId " +
        "AND o.quantity > 0 " +
        "ORDER BY o.createdAt ASC, o.id ASC")
    List<Order> findMatchingOrders(@Param("orderType") OrderType orderType,
        @Param("stockCode") String stockCode,
        @Param("price") BigDecimal price,
        @Param("excludeOrderId") Long excludeOrderId,
        @Param("excludeUserId") Long excludeUserId);

    /**
     * [B2] 조건부 UPDATE 로 주문 수량을 원자적으로 차감한다.
     *
     * <p>WHERE 절이 "아직 체결 가능하고 잔량이 예상과 같은가"를 DB 레벨에서
     * 원자적으로 판정한다. 조건 불충족 시 <b>영향 행 0</b> 을 반환하며,
     * 이는 오류가 아니라 "다른 트랜잭션이 먼저 소비함"이라는 정상 경합 결과다.
     *
     * <p>상태를 SET 안의 CASE 로 계산하지 않는 이유: MySQL 은 SET 절을
     * 왼쪽에서 오른쪽으로 평가하므로, quantity 를 먼저 차감하면 status 계산
     * 시점에 이미 값이 바뀌어 항상 PARTIAL 로 잘못 판정된다(1차 실험에서 실측).
     *
     * <p>clearAutomatically 를 쓰지 않는 이유는 B2-5 참조.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Order o SET "
        + "o.quantity = o.quantity - :qty, "
        + "o.status = :newStatus "
        + "WHERE o.id = :orderId "
        + "AND o.status IN ('PENDING', 'PARTIAL') "
        + "AND o.quantity = :expectedQty")
    int consumeQuantity(@Param("orderId") Long orderId,
        @Param("qty") int qty,
        @Param("expectedQty") int expectedQty,
        @Param("newStatus") Order.OrderStatus newStatus);

    // 2. 사용자 주문 내역 - 대기 중(PENDING)
    List<Order> findByUserAndStock_StockCodeAndStatus(User user, String stockCode,
        OrderStatus status);

    // 3. 사용자 주문 내역 - 체결됨 (COMPLETED or PARTIAL)
    List<Order> findByUserAndStock_StockCodeAndStatusIn(User user, String stockCode,
        List<OrderStatus> statuses);

    List<Order> findByUserAndStock_StockCode(User user, String stockCode);


        // 원금 (totalCash) = BUY + COMPLETED 주문의 quantity * price 총합
        @Query("SELECT SUM(o.price * o.quantity) " +
                "FROM Order o " +
                "WHERE o.user = :user " +
                "AND o.orderType = :orderType " +
                "AND o.status = :status")
        BigDecimal findTotalBuyAmountByUser(
                @Param("user") User user,
                @Param("orderType") Order.OrderType orderType,
                @Param("status") Order.OrderStatus status
        );


        List<Order> findByUser_UsernameAndOrderTypeAndStatus(String username, Order.OrderType orderType,
            Order.OrderStatus status);


    List<Order> findAllByUserAndStockStockCode(User user, String stockCode);


}

