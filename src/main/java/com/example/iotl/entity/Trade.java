package com.example.iotl.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long id;

    // 어떤 주문으로 체결되었는지 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "executed_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal executedPrice;

    @Column(name = "executed_quantity", nullable = false)
    private int executedQuantity;

    @Column(name = "executed_at", updatable = false)
    private LocalDateTime executedAt;

    @PrePersist
    protected void onExecute() {
        this.executedAt = LocalDateTime.now();
    }
}
