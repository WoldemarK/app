package com.example.individualsapi.rest;

import com.example.individuals_api.api.dto.AllUserInfirmationSystem;
import com.example.individuals_api.api.dto.TokenResponse;
import com.example.individuals_api.api.dto.UserLoginRequest;
import com.example.individuals_api.api.dto.UserRegistrationRequest;
import com.example.individualsapi.service.UserInfoService;
import com.example.individualsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final UserService userService;
    private final UserInfoService userInfoService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));

    }

    @PostMapping("/registration")
    public ResponseEntity<TokenResponse> createUser(@RequestBody UserRegistrationRequest registration) {
        TokenResponse tokenResponse = userService.registerUser(registration);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<AllUserInfirmationSystem> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userInfoService.getMe(jwt));
    }

}
