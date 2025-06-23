package com.example.iotl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_index")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String indexName;      // 예: 코스피, 코스닥 등
    private Double currentValue;   // 현재가
    private Double changeAmount;   // 변화량
    private Double changeRate;     // 변화율
    private String changeDirection;// "▲", "▼"

    private LocalDate date;        // 해당 데이터의 날짜

    @CreationTimestamp
    private LocalDateTime createdAt; // 데이터 저장 시각
}