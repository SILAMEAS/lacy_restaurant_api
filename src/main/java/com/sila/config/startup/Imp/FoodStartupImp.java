package com.sila.config.startup.Imp;

import com.sila.modules.food.model.Food;
import com.sila.modules.food.repository.FoodRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class FoodStartupImp {
    final FoodRepository foodRepository;

    public FoodStartupImp(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public void foodMigrate() {
        List<Food> foods = foodRepository.findAll();
        if (CollectionUtils.isEmpty(foods)) {
            return;
        }

        List<Food> toUpdateFoods = new ArrayList<>();

        for (Food food : foods) {
            if (food.getPrice() != null) {
                double calculated = food.getPriceWithDiscountCalculated();
                food.setPriceWithDiscount(calculated);
                toUpdateFoods.add(food);
            }
        }

        if (!CollectionUtils.isEmpty(toUpdateFoods)) {
            foodRepository.saveAll(toUpdateFoods);
        }
    }
}
