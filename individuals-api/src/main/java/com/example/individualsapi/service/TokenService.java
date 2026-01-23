package com.example.individualsapi.service;

import com.example.individual.dto.TokenRefreshRequest;
import com.example.individual.dto.TokenResponse;
import com.example.individual.dto.UserLoginRequest;
import com.example.individualsapi.client.KeycloakClient;
import com.example.individualsapi.mapper.KeycloakMapper;
import com.example.individualsapi.mapper.TokenResponseMapper;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakClient keycloakClient;
    private final KeycloakMapper keycloakMapper;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan("tokenService.login")
    public Mono<TokenResponse> login(UserLoginRequest userLoginRequest) {
        var kcUserLoginRequest = keycloakMapper.toKeycloakUserLoginRequest(userLoginRequest);
        return keycloakClient.login(kcUserLoginRequest)
                .doOnNext(t -> log.info("Token successfully generated for email = [{}]", userLoginRequest.getEmail()))
                .doOnError(e -> log.error("Failed to generate token for email = [{}]", userLoginRequest.getEmail()))
                .map(tokenResponseMapper::toTokenResponse);
    }

    @WithSpan("tokenService.refreshToken")
    public Mono<TokenResponse> refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        var kcTokenRefreshRequest = keycloakMapper.toKeycloakTokenRefreshRequest(tokenRefreshRequest);
        return keycloakClient.refreshToken(kcTokenRefreshRequest)
                .doOnNext(r -> log.info("Token refreshed successfully"))
                .map(tokenResponseMapper::toTokenResponse);
    }

    @WithSpan("tokenService.obtainAdminToken")
    public Mono<TokenResponse> obtainAdminToken() {
        return keycloakClient.adminLogin()
                .doOnNext(t -> log.info("Admin token obtained"))
                .map(tokenResponseMapper::toTokenResponse);
    }
}
