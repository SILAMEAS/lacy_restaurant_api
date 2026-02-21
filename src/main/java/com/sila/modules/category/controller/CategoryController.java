package com.sila.modules.category.controller;

import com.sila.modules.category.dto.CategoryRequest;
import com.sila.modules.category.dto.CategoryResponse;
import com.sila.modules.category.model.Category;
import com.sila.modules.category.services.CategoryService;
import com.sila.modules.chat.dto.MessageResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category Controller")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;

    @GetMapping()
    public ResponseEntity<EntityResponseHandler<CategoryResponse>> getCategories(@ModelAttribute PaginationRequest request) {
        return ResponseEntity.ok(categoryService.gets(request));
    }

    @PostMapping
    @PreAuthorization({ROLE.ADMIN, ROLE.OWNER})
    public ResponseEntity<Category> createCategory(@Validated(OnCreate.class) @ModelAttribute CategoryRequest request) {
        return new ResponseEntity<>(categoryService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<CategoryResponse>> getRestaurantCategory(@PathVariable Long restaurantId) {
        List<CategoryResponse> categoriesInRestaurant = categoryService.getsByResId(restaurantId).stream().map(f -> modelMapper.map(f, CategoryResponse.class)).toList();
        return new ResponseEntity<>(categoriesInRestaurant, HttpStatus.OK);
    }

    @PreAuthorization({ROLE.ADMIN, ROLE.OWNER})
    @PutMapping("{categoryId}")
    public ResponseEntity<Category> editCategory(@Validated(OnUpdate.class) @ModelAttribute CategoryRequest request, @PathVariable Long categoryId) {
        return new ResponseEntity<>(categoryService.update(request, categoryId), HttpStatus.CREATED);
    }

    @PreAuthorization({ROLE.ADMIN, ROLE.OWNER})
    @DeleteMapping("{categoryId}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable Long categoryId) {
        return new ResponseEntity<>(categoryService.delete(categoryId), HttpStatus.CREATED);
    }

    @PreAuthorization({ROLE.ADMIN})
    @DeleteMapping("/bulk")
    public ResponseEntity<String> deleteAll() {
        return new ResponseEntity<>(categoryService.deleteAllCategories(), HttpStatus.CREATED);
    }


}
