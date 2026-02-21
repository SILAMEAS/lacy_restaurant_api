package com.sila.modules.food.controller;

import com.sila.modules.category.model.Category;
import com.sila.modules.category.services.CategoryService;
import com.sila.modules.chat.dto.MessageResponse;
import com.sila.modules.food.dto.FoodRequest;
import com.sila.modules.food.dto.FoodResponse;
import com.sila.modules.food.model.Food;
import com.sila.modules.food.services.FoodService;
import com.sila.modules.resturant.model.Restaurant;
import com.sila.modules.resturant.services.RestaurantService;
import com.sila.share.annotation.PreAuthorization;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.enums.ROLE;
import com.sila.share.method.OnCreate;
import com.sila.share.method.OnUpdate;
import com.sila.share.pagination.EntityResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Food Controller")
@RestController
@RequestMapping("api/foods")
@RequiredArgsConstructor
@Slf4j
public class FoodController {
    private final FoodService foodService;
    private final ModelMapper modelMapper;
    private final RestaurantService restaurantService;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<EntityResponseHandler<FoodResponse>> listFoods(@ModelAttribute PaginationRequest request) {
        return ResponseEntity.ok(foodService.gets(request));
    }

    @PreAuthorization({ROLE.OWNER})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> createFood(
            @Validated(OnCreate.class) @ModelAttribute FoodRequest foodRequest,
            @RequestParam("images") List<MultipartFile> imageFiles) throws Exception {

        Restaurant restaurant = restaurantService.getById(foodRequest.getRestaurantId());
        Category category = categoryService.getById(foodRequest.getCategoryId());
        foodService.create(foodRequest, category, restaurant, imageFiles);

        return new ResponseEntity<>(MessageResponse.builder().message("food create").status(201).build(), HttpStatus.CREATED);
    }

    @PreAuthorization({ROLE.OWNER})
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFood(
            @PathVariable Long id) {

        foodService.delete(id);

        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @GetMapping("restaurant/{restaurantId}")
    public ResponseEntity<EntityResponseHandler<FoodResponse>> listFoodByRestaurantId(@ModelAttribute PaginationRequest request, @PathVariable Long restaurantId) throws Exception {

        var foodResEntityResponseHandler = foodService.getsByResId(restaurantId, request);
        return new ResponseEntity<>(foodResEntityResponseHandler, HttpStatus.OK);
    }

    @PreAuthorization({ROLE.OWNER, ROLE.ADMIN})
    @PutMapping("/{foodId}")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable Long foodId,
            @Validated(OnUpdate.class) @ModelAttribute FoodRequest foodRequest) {

        Food updatedFood = foodService.update(foodRequest, foodId);

        return new ResponseEntity<>(modelMapper.map(updatedFood, FoodResponse.class), HttpStatus.OK);
    }

    @PreAuthorization({ROLE.ADMIN, ROLE.OWNER})
    @PutMapping("/{id}/availability-status")
    public ResponseEntity<Food> updateAvailabilityStatus(
            @PathVariable Long id) {
        Food updatedFood = foodService.updateStatus(id);

        return new ResponseEntity<>(updatedFood, HttpStatus.OK);
    }
}
