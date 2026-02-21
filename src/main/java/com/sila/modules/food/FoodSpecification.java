package com.sila.modules.food;

import com.sila.modules.category.model.Category_;
import com.sila.modules.food.model.Food;
import com.sila.modules.food.model.Food_;
import com.sila.modules.resturant.model.Restaurant_;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)

public final class FoodSpecification {
    public static Specification<Food> search(String search) {
        if (search == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Food_.NAME), search + "%");

    }

    public static Specification<Food> filterCategory(String category) {
        if (category == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Food_.category).get(Category_.NAME), category);

    }


    public static Specification<Food> filterByRestaurantId(Long resId) {
        if (resId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Food_.RESTAURANT).get(Restaurant_.ID), resId);

    }

    /**
     * ==============================  Specification  ========================================================
     **/
    public static Specification<Food> filterFood(String search, String filterBy) {
        Specification<Food> spec = Specification.where(null);
        spec = addSearchSpecification(spec, search);
        spec = addFilterSpecification(spec, filterBy);
        return spec;
    }

    public static Specification<Food> filterFoodByRestaurantId(Long restaurantId, String filterBy) {
        Specification<Food> spec = Specification.where(null);
        spec = addFilterSpecification(spec, filterBy);
        spec = addRestaurantIdSpecification(spec, restaurantId);
        return spec;
    }

    /**
     * ==============================  Re-usable method  ========================================================
     **/
    private static Specification<Food> addRestaurantIdSpecification(Specification<Food> spec, Long restaurantId) {
        if (Objects.nonNull(restaurantId)) {
            return spec.and(FoodSpecification.filterByRestaurantId(restaurantId));
        }
        return spec;
    }

    public static Specification<Food> addSearchSpecification(Specification<Food> spec, String search) {
        if (Objects.nonNull(search)) {
            return spec.and(FoodSpecification.search(search));
        }
        return spec;
    }

    private static Specification<Food> addFilterSpecification(Specification<Food> spec, String filterBy) {
        if (Objects.nonNull(filterBy)) {
            return spec.and(FoodSpecification.filterCategory(filterBy));
        }
        return spec;
    }

    public static Specification<Food> filterByPrice(Double price) {
        if (price == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(Food_.PRICE), price);
    }

    public static Specification<Food> filterByFoodType(String foodType) {
        if (foodType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(Food_.FOODTYPE), foodType);
    }

    public static Specification<Food> filterByPriceRange(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get(Food_.PRICE), minPrice, maxPrice);
            } else if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get(Food_.PRICE), minPrice);
            } else if (maxPrice != null) {
                return cb.lessThanOrEqualTo(root.get(Food_.PRICE), maxPrice);
            }
            return null;
        };
    }


}