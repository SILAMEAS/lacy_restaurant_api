package com.sila.modules.order.repository;

import com.sila.modules.order.model.Order;
import com.sila.modules.profile.model.User;
import com.sila.modules.resturant.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT DISTINCT o.user FROM Order o WHERE o.restaurant.id = :restaurantId")
    List<User> findUsersByRestaurantId(@Param("restaurantId") Long restaurantId);

    Page<Order> findAllByUser(User user, Pageable pageable);

    void deleteAllByRestaurant(Restaurant restaurant);

    List<Order> findAllByRestaurant(Restaurant restaurant);

    Page<Order> findAllByRestaurant(Restaurant restaurant, Pageable pageable);
}
