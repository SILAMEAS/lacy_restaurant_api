package com.sila.modules.food.dto;

import com.sila.modules.category.model.Category;
import com.sila.share.method.OnCreate;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FoodRequest {
    private String name;
    private String description;
    private Double price;
    private Category category;
    @NotEmpty(groups = OnCreate.class, message = "images must not be empty during creation")
    private List<MultipartFile> images;
    private Long restaurantId;
    private Long categoryId;
    @NotEmpty(groups = OnCreate.class, message = "foodType must not be empty during creation")
    private String foodType;
    private boolean available;
    private double tax = 0;
    private double discount = 0;

}
