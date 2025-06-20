package com.example.iotl.repository;

import com.example.iotl.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade,Long> {

    @Query("SELECT t FROM Trade t " +
            "WHERE t.order.user.userId = :userId " +
            "AND t.executedAt BETWEEN :start AND :end")
    List<Trade> findTradesByUserAndDateRange(@Param("userId") Long userId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
