package com.sila.modules.cart.dto;

import com.sila.modules.cart.model.CartItem;
import com.sila.modules.food.dto.FoodResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CartItemResponse {
    private Long id;
    private int quantity;
    private FoodResponse food;

    public static CartItemResponse toResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .food(FoodResponse.toResponse(cartItem.getFood()))
                .quantity(cartItem.getQuantity())
                .build();
    }
}
