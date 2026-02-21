package com.sila.modules.food.dto;

import com.sila.modules.food.FoodType;
import com.sila.modules.food.model.Food;
import com.sila.modules.food.model.ImageFood;
import com.sila.modules.resturant.dto.DiscountResponse;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class FoodResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Double price;

    private Double priceDiscount;

    private List<String> images;
    private Long restaurantId;
    private String restaurantName;
    private transient CategoryDTO category;
    private FoodType foodType;
    private boolean available;
    private double deliveryFee;
    private DiscountResponse discount;

    private boolean open;

    private double tax;

    public static FoodResponse toResponse(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .restaurantName(food.getRestaurant().getName())
                .description(food.getDescription())
                .price(food.getPrice())
                .category(new CategoryDTO(
                        food.getCategory().getId(),
                        food.getCategory().getName()))
                .images(food.getImages().stream()
                        .map(ImageFood::getUrl) // assuming `getUrl()` exists in ImageFood
                        .toList())
                .available(food.isAvailable())
                .restaurantId(food.getRestaurant().getId()) // optional simplification
                .foodType(food.getFoodtype())
                .tax(food.getTax())
                .deliveryFee(food.getRestaurant().getDeliveryFee())
                .priceDiscount(food.getPriceWithDiscount())
                .open(food.getRestaurant().isOpen())
                .discount(DiscountResponse.builder()
                        .food(food.getDiscount())
                        .restaurant(food.getRestaurant().getRestaurantDiscount())
                        .total(food.getTotalDiscount())
                        .build())
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDTO {
        private Long id;
        private String name;
    }
}


