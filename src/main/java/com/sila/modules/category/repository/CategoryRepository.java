package com.sila.modules.category.repository;

import com.sila.modules.category.model.Category;
import com.sila.modules.resturant.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    public List<Category> findByRestaurantId(Long restaurant_id);

    boolean existsByNameAndRestaurant(String name, Restaurant restaurant);

    void deleteByRestaurantId(Long restaurant_id);

    @Query("""
                SELECT DISTINCT c FROM Category c
                LEFT JOIN c.foodList f
                WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
                  AND (:restaurantId IS NULL OR c.restaurant.id = :restaurantId)
            """)
    Page<Category> findByFilters(@Param("name") String name,
                                 @Param("restaurantId") Long restaurantId,
                                 Pageable pageable);

    Optional<Category> findByName(String filterBy);
}
