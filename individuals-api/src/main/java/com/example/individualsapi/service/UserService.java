package com.example.individualsapi.service;

import com.example.individuals.dto.IndividualWriteDto;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserInfoResponse;
import com.example.individuals.dto.UserLoginRequest;
import com.example.individualsapi.client.KeycloakClient;
import com.example.individualsapi.dto.KeycloakCredentialsRepresentation;
import com.example.individualsapi.dto.KeycloakUserRepresentation;
import com.example.individualsapi.exception.BadRequestException;
import com.example.individualsapi.mapper.TokenResponseMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final KeycloakClient keycloakClient;
    private final PersonService personService;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan(value = "userService.getCurrentUserInfo")
    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new BadRequestException("No authentication present")))
                .flatMap(ctx -> extractUserInfo(ctx.getAuthentication()));
    }

    private static Mono<UserInfoResponse> extractUserInfo(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt principal)) {
            log.error("Authentication principal is not a Jwt");
            return Mono.error(new BadRequestException("Authentication principal is not a Jwt"));
        }
        UserInfoResponse response = new UserInfoResponse();
        response.id(principal.getSubject());
        response.email((String) principal.getClaims().get("email"));
        response.roles(principal.getClaimAsStringList("roles"));
        response.createdAt(principal.getIssuedAt() != null ? principal.getIssuedAt().atOffset(ZoneOffset.UTC) : null);
        log.info("getUserInfoResponseMono: {}", response);
        return Mono.just(response);

    }

    @WithSpan("userService.register")
    public Mono<TokenResponse> register(IndividualWriteDto request) {
        return personService.register(request)
                .flatMap(personId ->
                        performKeycloakRegistration(request)
                                .onErrorResume(err ->
                                        personService.compensateRegistration(personId.getId().toString())
                                                .then(Mono.error(err))
                                )
                )
                .map(tokenResponseMapper::toTokenResponse);
    }

    private Mono<TokenResponse> performKeycloakRegistration(IndividualWriteDto request) {
        return keycloakClient.adminLogin()
                .flatMap(adminToken ->
                        createUserInKeycloak(adminToken, request)
                                .flatMap(kcUserId -> setPasswordInKeycloak(adminToken, kcUserId, request.getPassword()))
                                .flatMap(kcUserId -> loginNewUser(request.getEmail(), request.getPassword()))
                );
    }

    private Mono<String> createUserInKeycloak(TokenResponse adminToken, IndividualWriteDto request) {
        KeycloakUserRepresentation user = new KeycloakUserRepresentation(
                null,
                request.getLastName(),
                request.getEmail(),
                true,
                true,
                null
        );
        return keycloakClient.registerUser(adminToken, user);
    }

    private Mono<String> setPasswordInKeycloak(TokenResponse adminToken, String kcUserId, String password) {
        KeycloakCredentialsRepresentation cred = new KeycloakCredentialsRepresentation(
                "password", password, false
        );
        return keycloakClient.resetUserPassword(kcUserId, cred, adminToken.getAccessToken())
                .thenReturn(kcUserId);
    }

    private Mono<TokenResponse> loginNewUser(String email, String password) {
        return keycloakClient.login(new UserLoginRequest(email, password));
    }
}