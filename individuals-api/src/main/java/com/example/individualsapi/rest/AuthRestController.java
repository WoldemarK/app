package com.example.individualsapi.rest;

import com.example.individuals.dto.*;
import com.example.individualsapi.service.TokenService;
import com.example.individualsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthRestController {

    private final UserService userService;
    private final TokenService tokenService;

    @GetMapping("/me")
    public Mono<UserInfoResponse> getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping(value = "/registration")
    public Mono<ResponseEntity<TokenResponse>> registration(@RequestBody Mono<IndividualWriteDto> individualWriteDto) {
        return individualWriteDto.flatMap(userService::register)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(token));
    }
    @PostMapping(value = "/login")
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody Mono<UserLoginRequest> userLoginRequestMono) {
        return userLoginRequestMono.flatMap(tokenService::login)
                .map(ResponseEntity::ok);
    }
    @PostMapping(value = "/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@Valid @RequestBody Mono<TokenRefreshRequest> tokenRefreshRequestMono) {
        return tokenRefreshRequestMono.flatMap(tokenService::refreshToken)
                .map(ResponseEntity::ok);
    }
}