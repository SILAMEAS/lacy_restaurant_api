package com.sila.modules.favorite.dto;

import com.sila.modules.favorite.model.Favorite;
import com.sila.modules.resturant.model.ImageRestaurant;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteResponse {
    private Long id;
    private String name;
    private String description;
    private Long restaurantId;
    private List<ImageRestaurant> images;

    public static FavoriteResponse toResponse(Favorite favorite) {

        return FavoriteResponse.builder()
                .id(favorite.getId())
                .name(favorite.getName())
                .description(favorite.getDescription())
                .images(favorite.getRestaurant().getImages())
                .restaurantId(favorite.getRestaurant().getId())
                .build();

    }
}
