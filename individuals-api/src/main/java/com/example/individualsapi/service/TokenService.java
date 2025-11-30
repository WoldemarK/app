package com.example.individualsapi.service;

import com.example.individuals.dto.TokenRefreshRequest;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserLoginRequest;
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
    public Mono<TokenResponse> login(UserLoginRequest loginRequest) {
        UserLoginRequest keycloakRequest = keycloakMapper.toKeycloakUserLoginRequest(loginRequest);
        return keycloakClient.login(keycloakRequest)
                .doOnNext(t ->
                        log.info("Token successfully generated for email = [{}]", keycloakRequest.getEmail()))
                .doOnError(e ->
                        log.error("Failed to generate token for email = [{}]", keycloakRequest.getEmail(), e))
                .map(tokenResponseMapper::toTokenResponse);
    }

    @WithSpan("tokenService.refreshToken")
    public Mono<TokenResponse> refreshToken(TokenRefreshRequest refresh) {
        TokenRefreshRequest keycloakRefreshRequest = keycloakMapper.toKeycloakTokenRefreshRequest(refresh);
        return keycloakClient.refreshToken(keycloakRefreshRequest)
                .doOnNext(r -> log.info("Token refreshed successfully"))
                .map(tokenResponseMapper::toTokenResponse);
    }

    @WithSpan("tokenService.obtainAdminToken")
    public Mono<TokenResponse> obtainAdminToken() {
        return keycloakClient.adminLogin()
                .doOnNext(t -> log.info("Admin token obtained for realm = [{}]", "master"))
                .doOnError(e -> log.error("Failed to obtain admin token", e))
                .map(tokenResponseMapper::toTokenResponse);
    }
}
