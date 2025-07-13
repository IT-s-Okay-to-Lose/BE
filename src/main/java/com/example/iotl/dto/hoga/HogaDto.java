package com.example.iotl.dto.hoga;

import com.example.iotl.domain.hoga.HogaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HogaDto {
    private int price;
    private int quantity;
    private HogaType type;
}
