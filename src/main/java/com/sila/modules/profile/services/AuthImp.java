package com.sila.modules.profile.services;

import com.sila.config.custom.CustomUserDetails;
import com.sila.config.custom.CustomerUserDetailsService;
import com.sila.config.exception.BadRequestException;
import com.sila.config.exception.NotFoundException;
import com.sila.config.jwt.JwtProvider;
import com.sila.modules.profile.dto.req.LoginRequest;
import com.sila.modules.profile.dto.req.SignUpRequest;
import com.sila.modules.profile.dto.res.LoginResponse;
import com.sila.modules.profile.model.User;
import com.sila.modules.profile.repository.UserRepository;
import com.sila.modules.resturant.services.RestaurantService;
import com.sila.share.enums.ROLE;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthImp implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomerUserDetailsService customerUserDetailsService;
    private final UserService userService;
    private final RestaurantService restaurantService;

    private Authentication authenticate(String email, String password) {
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(email);
        if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new NotFoundException("Invalid email or password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public ResponseEntity<String> signUp(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new BadRequestException("Email is already used");
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setRole(request.getRole());

        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(newUser);

        if (request.getRole() == ROLE.OWNER) {
            restaurantService.autoCreateRestaurantAsDefault(newUser);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    public ResponseEntity<LoginResponse> signIn(LoginRequest req) {
        Authentication authentication = authenticate(req.getEmail(), req.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication); // Access token
        String refreshToken = jwtProvider.generateRefreshToken(authentication); // Generate refresh token


        User user = userService.getByEmail(req.getEmail());
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();

        LoginResponse response = LoginResponse.builder().
                accessToken(jwt).
                refreshToken(refreshToken).
                userId(user.getId()).
                role(ROLE.valueOf(role)).
                message("Login successfully").build();
        return ResponseEntity.ok(response); // Return response
    }


    public ResponseEntity<LoginResponse> refreshToken(String refreshToken) {

        if (jwtProvider.validateRefreshToken(refreshToken)) {
            String email = jwtProvider.getEmailFromJwtToken(refreshToken);
            CustomUserDetails userDetails = (CustomUserDetails) customerUserDetailsService.loadUserByUsername(email); // Use the custom UserDetails

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            String newAccessToken = jwtProvider.generateToken(auth);
            String newRefreshToken = jwtProvider.generateRefreshToken(auth); // Generate a new refresh token

            LoginResponse response = LoginResponse.builder().
                    accessToken(newAccessToken).
                    refreshToken(newRefreshToken).
                    userId(userDetails.user().getId()).
                    role(ROLE.valueOf(userDetails.getAuthorities().iterator().next().getAuthority())).
                    message("Token refreshed successfully").build();
            return ResponseEntity.ok(response);
        } else {
            log.warn("Invalid refresh token: {}", refreshToken); // Log invalid token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }


}
