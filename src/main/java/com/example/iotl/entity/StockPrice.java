package com.example.iotl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_price")
@Getter
@Setter
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "open_price")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    private BigDecimal lowPrice;

    @Column(name = "close_price")
    private BigDecimal closePrice;

    @Column(name = "price_diff")
    private BigDecimal priceDiff;

    @Column(name = "price_rate")
    private BigDecimal priceRate;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "prev_close_price")
    private BigDecimal prevClosePrice;

    @Column(name = "price_sign")
    private Byte priceSign;

    public StockPrice() {}
}