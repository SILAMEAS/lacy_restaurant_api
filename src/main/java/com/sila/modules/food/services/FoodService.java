package com.sila.modules.food.services;

import com.sila.modules.category.model.Category;
import com.sila.modules.food.dto.FoodRequest;
import com.sila.modules.food.dto.FoodResponse;
import com.sila.modules.food.model.Food;
import com.sila.modules.resturant.model.Restaurant;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.pagination.EntityResponseHandler;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService {
    Food create(FoodRequest food, Category category, Restaurant restaurant, List<MultipartFile> imageFiles);

    Food update(FoodRequest food, Long foodId);

    void delete(Long id);

    String deleteByCategoryId(Long categoryId);

    Food getById(Long foodId);

    Food updateStatus(Long id);

    EntityResponseHandler<FoodResponse> gets(PaginationRequest request);

    Long count();

    Long count(Long restaurantId);

    EntityResponseHandler<FoodResponse> getsByResId(Long restaurantId, PaginationRequest request);

}
