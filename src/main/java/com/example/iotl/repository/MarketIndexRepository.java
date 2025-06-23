package com.example.iotl.repository;

import com.example.iotl.entity.MarketIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Long> {
    Optional<MarketIndex> findByIndexNameAndDate(String indexName, LocalDate date);
}