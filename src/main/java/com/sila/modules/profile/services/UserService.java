package com.sila.modules.profile.services;

import com.sila.modules.profile.dto.req.UpdateUserRequest;
import com.sila.modules.profile.dto.req.UserRequest;
import com.sila.modules.profile.dto.res.UserResponse;
import com.sila.modules.profile.model.User;
import com.sila.share.pagination.EntityResponseHandler;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User getByJwt(String jwt) throws Exception;

    User getByEmail(String email);

    User getById(Long userId);

    EntityResponseHandler<UserResponse> list(Pageable pageable, String search) throws Exception;

    UserResponse update(UserRequest userReq) throws Exception;

    UserResponse getProfile() throws Exception;

    Long count();

    EntityResponseHandler<UserResponse.UserResponseCustom> getUsersWhoOrderedFromRestaurant(Long restaurantId, Pageable pageable);

    Long countById(Long restaurantId);

    String updateUser(Long Id, UpdateUserRequest request);

    String deleteUser(Long Id);
}
