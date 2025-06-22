package com.example.iotl.repository;

import com.example.iotl.entity.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    Optional<Exchange> findByBaseCodeAndTargetCodeAndDate(String baseCode, String targetCode, LocalDate date);

    boolean existsByBaseCodeAndTargetCodeAndDate(String baseCode, String targetCode, LocalDate date); // ✅ 추가
}