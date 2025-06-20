package com.example.iotl.repository;

import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.User;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. 주문 매칭 (체결용)
    @Query("SELECT o FROM Order o " +
        "WHERE o.status = 'PENDING' " +
        "AND o.orderType = :orderType " +
        "AND o.stock.stockCode = :stockCode " +
        "AND o.price = :price " +
        "ORDER BY o.createdAt ASC")
    List<Order> findMatchingOrders(@Param("orderType") OrderType orderType,
        @Param("stockCode") String stockCode,
        @Param("price") BigDecimal price);

    // 2. 사용자 주문 내역 - 대기 중(PENDING)
    List<Order> findByUserAndStock_StockCodeAndStatus(User user, String stockCode, OrderStatus status);

    // 3. 사용자 주문 내역 - 체결됨 (COMPLETED or PARTIAL)
    List<Order> findByUserAndStock_StockCodeAndStatusIn(User user, String stockCode, List<OrderStatus> statuses);

    List<Order> findByUserAndStock_StockCode(User user, String stockCode);





}
