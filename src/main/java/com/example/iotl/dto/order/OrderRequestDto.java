package com.example.iotl.dto.order;

import com.example.iotl.entity.Order.OrderType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OrderRequestDto {

    private String stockCode;
    private int quantity;
    private OrderType orderType;
    private BigDecimal price;

}
