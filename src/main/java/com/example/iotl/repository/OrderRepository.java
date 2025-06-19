package com.example.iotl.repository;

import com.example.iotl.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    // 원금 (totalCash) = BUY + COMPLETED 주문의 quantity * price 총합
    @Query("SELECT SUM(o.price * o.quantity) " +
            "FROM Order o " +
            "WHERE o.user.userId = :userId " +
            "AND o.orderType = :orderType " +
            "AND o.status = :status")
    BigDecimal findTotalBuyAmountByUserId(
            @Param("userId") Long userId,
            @Param("orderType") Order.OrderType orderType,
            @Param("status") Order.OrderStatus status
    );

    List<Order> findByUser_UserIdAndOrderTypeAndStatus(Long userId, Order.OrderType orderType, Order.OrderStatus status);
}
