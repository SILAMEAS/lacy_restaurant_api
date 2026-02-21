package com.sila.modules.profile.controller;

import com.sila.config.exception.AccessDeniedException;
import com.sila.modules.profile.dto.req.LoginRequest;
import com.sila.modules.profile.dto.req.SignUpRequest;
import com.sila.modules.profile.dto.res.LoginResponse;
import com.sila.modules.profile.services.AuthService;
import com.sila.share.enums.ROLE;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Tag(name = "Auth Controller", description = "Operations related to Auth")
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Valid SignUpRequest request) {
        if (request.getRole().equals(ROLE.USER)) {
            return authService.signUp(request);
        } else {
            throw new AccessDeniedException("Only users can sign up");
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<LoginResponse> signIn(@RequestBody LoginRequest loginReq) {
        return authService.signIn(loginReq);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return authService.refreshToken(refreshToken);
    }

    @Hidden
    @GetMapping("/test-api")
    public ResponseEntity<String> getTest() {
        return new ResponseEntity<>("Api working", HttpStatus.OK);
    }
}
