package com.sila.modules.resturant.controller;

import com.sila.modules.chat.dto.MessageResponse;
import com.sila.modules.favorite.dto.FavoriteResponse;
import com.sila.modules.profile.services.UserService;
import com.sila.modules.resturant.dto.RestaurantRequest;
import com.sila.modules.resturant.dto.RestaurantResponse;
import com.sila.modules.resturant.services.RestaurantService;
import com.sila.share.annotation.PreAuthorization;
import com.sila.share.dto.req.SearchRequest;
import com.sila.share.enums.ROLE;
import com.sila.share.method.OnCreate;
import com.sila.share.method.OnUpdate;
import com.sila.share.pagination.EntityResponseHandler;
import com.sila.share.pagination.PaginationDefaults;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sila.modules.resturant.services.RestaurantImp.mapToRestaurantResponse;

@Tag(name = "Restaurant Controller")
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<EntityResponseHandler<RestaurantResponse>> searchRestaurants(
            @RequestParam(defaultValue = PaginationDefaults.PAGE_NO) Integer pageNo,
            @RequestParam(defaultValue = PaginationDefaults.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = PaginationDefaults.SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationDefaults.SORT_ORDER) String sortOrder,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Direction.valueOf(sortOrder.toUpperCase()), sortBy));
        SearchRequest searchReq = new SearchRequest();
        searchReq.setSearch(search);
        return new ResponseEntity<>(restaurantService.search(pageable, searchReq), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) throws Exception {
        return new ResponseEntity<>(mapToRestaurantResponse(restaurantService.getById(id)), HttpStatus.OK);
    }

    @PutMapping("/{id}/favorites")
    public ResponseEntity<List<FavoriteResponse>> addRestaurantToFavorites(@PathVariable Long id) {
        return new ResponseEntity<>(restaurantService.addFav(id), HttpStatus.OK);
    }

    @PreAuthorization({ROLE.OWNER})
    @PostMapping()
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Validated(OnCreate.class) @ModelAttribute RestaurantRequest restaurantReq) {
        return new ResponseEntity<>(restaurantService.create(restaurantReq), HttpStatus.CREATED);
    }

    @PreAuthorization({ROLE.OWNER})
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @Validated(OnUpdate.class) @ModelAttribute RestaurantRequest restaurantReq,
            @PathVariable Long id) throws Exception {
        return new ResponseEntity<>(restaurantService.update(restaurantReq, id), HttpStatus.OK);
    }

    @PreAuthorization({ROLE.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteRestaurant(@PathVariable @Valid Long id) throws Exception {
        return new ResponseEntity<>(restaurantService.delete(id), HttpStatus.OK);
    }

    @PreAuthorization({ROLE.ADMIN, ROLE.OWNER})
    @GetMapping("/owner")
    public ResponseEntity<RestaurantResponse> findRestaurantByOwnerLogin() {
        return new ResponseEntity<>(this.modelMapper.map(restaurantService.getByUserLogin(), RestaurantResponse.class), HttpStatus.OK);
    }
}

