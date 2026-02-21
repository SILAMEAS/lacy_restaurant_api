package com.sila.modules.resturant.services;

import com.sila.modules.chat.dto.MessageResponse;
import com.sila.modules.favorite.dto.FavoriteResponse;
import com.sila.modules.profile.dto.res.UserResponse;
import com.sila.modules.profile.model.User;
import com.sila.modules.resturant.dto.RestaurantRequest;
import com.sila.modules.resturant.dto.RestaurantResponse;
import com.sila.modules.resturant.model.Restaurant;
import com.sila.share.dto.req.SearchRequest;
import com.sila.share.pagination.EntityResponseHandler;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface RestaurantService {
    RestaurantResponse create(RestaurantRequest restaurantRequest);

    RestaurantResponse update(RestaurantRequest updateRestaurant, Long restaurantId) throws Exception;

    MessageResponse delete(Long id);

    Restaurant getById(Long id);

    RestaurantResponse getByUserLogin();

    List<FavoriteResponse> addFav(Long restaurantId);

    EntityResponseHandler<RestaurantResponse> search(Pageable pageable, SearchRequest searchReq);

    List<UserResponse> getUsersWhoOrderedFromRestaurant(Long restaurantId);

    Long count();

    Restaurant findRestaurantByOwner(User user);

    void autoCreateRestaurantAsDefault(User user);
}
