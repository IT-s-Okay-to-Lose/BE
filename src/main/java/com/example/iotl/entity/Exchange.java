package com.example.iotl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "exchange", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"base_code", "target_code", "date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Exchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_code", nullable = false)
    private String baseCode;

    @Column(name = "target_code", nullable = false)
    private String targetCode;

    @Column(nullable = false)
    private double rate;

    @Column(nullable = false)
    private LocalDate date;
}