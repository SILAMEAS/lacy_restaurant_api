package com.sila.modules.profile.services;

import com.sila.config.context.UserContext;
import com.sila.config.exception.BadRequestException;
import com.sila.config.exception.NotFoundException;
import com.sila.config.jwt.JwtProvider;
import com.sila.modules.profile.UserSpecification;
import com.sila.modules.profile.dto.req.UpdateUserRequest;
import com.sila.modules.profile.dto.req.UserRequest;
import com.sila.modules.profile.dto.res.UserResponse;
import com.sila.modules.profile.model.User;
import com.sila.modules.profile.repository.UserRepository;
import com.sila.share.Utils;
import com.sila.share.pagination.EntityResponseHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ModelMapper modelMapper;

    @Override
    public User getByJwt(String jwt) {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        return getByEmail(email);
    }


    @Override
    public User getByEmail(String email) {
        User foundUser = userRepository.findByEmail(email);
        if (foundUser == null) {
            throw new NotFoundException("User not found");
        }
        return foundUser;
    }

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Override
    public EntityResponseHandler<UserResponse> list(Pageable pageable, String search) {
        Specification<User> spec = Specification.where(null);
        if (search != null) {
            spec = spec.and(UserSpecification.search(search));
        }
        return new EntityResponseHandler<>(userRepository.findAll(spec, pageable).map(re -> this.modelMapper.map(re, UserResponse.class)));
    }

    @Override
    public EntityResponseHandler<UserResponse.UserResponseCustom> getUsersWhoOrderedFromRestaurant(Long restaurantId, Pageable pageable) {
        Specification<User> spec = UserSpecification.hasOrderedFromRestaurant(restaurantId);
        return new EntityResponseHandler<>(userRepository.findAll(spec, pageable).map(UserResponse::toUserResponseCustom));
    }

    @Override
    public Long countById(Long restaurantId) {
        return userRepository.count(UserSpecification.hasOrderedFromRestaurant(restaurantId));
    }

    @Override
    public String updateUser(Long Id, UpdateUserRequest request) {
        var userExit = userRepository.findById(Id).orElseThrow(() -> new BadRequestException("User not found"));

        userExit.setRole(request.getRole());
        userExit.setFullName(request.getFullName());

        userRepository.save(userExit);

        return "User updated successfully";
    }

    @Override
    public String deleteUser(Long Id) {
        userRepository.deleteById(Id);
        return "Successfully deleted user";
    }


    @Override
    public UserResponse update(UserRequest userReq) {
        var user = UserContext.getUser();

        Utils.setValueSafe(userReq.getProfile(), user::setProfile);
        Utils.setValueSafe(userReq.getAddresses(), user::setAddresses);
        Utils.setValueSafe(userReq.getFullName(), user::setFullName);


        return this.modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    @Transactional
    @Override
    public UserResponse getProfile() throws Exception {
        User userFromContext = UserContext.getUser(); // JWT-based context
        User user = userRepository.findByIdWithFavorites(userFromContext.getId())
                .orElseThrow(() -> new Exception("User not found"));
        return this.modelMapper.map(user, UserResponse.class);
    }

    @Override
    public Long count() {
        return userRepository.count();
    }


}
