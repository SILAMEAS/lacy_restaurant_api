package com.sila.modules.resturant.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class DiscountResponse {
    private double total;
    private double restaurant;
    private double food;
}
