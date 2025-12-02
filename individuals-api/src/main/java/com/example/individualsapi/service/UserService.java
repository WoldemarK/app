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
                        keycloakClient.adminLogin()
                                .flatMap(adminToken ->
                                        keycloakClient.registerUser(adminToken, new KeycloakUserRepresentation(
                                                        null,
                                                        request.getLastName(),
                                                        request.getEmail(),
                                                        true,
                                                        true,
                                                        null
                                                ))
                                                .flatMap(kcUserId -> {
                                                    KeycloakCredentialsRepresentation cred = new KeycloakCredentialsRepresentation(
                                                            "password", request.getPassword(), false
                                                    );
                                                    return keycloakClient.resetUserPassword(
                                                                    kcUserId, cred, adminToken.getAccessToken()
                                                            )
                                                            .thenReturn(kcUserId);
                                                })
                                                .flatMap(_ ->
                                                        keycloakClient.login(new UserLoginRequest(
                                                                request.getEmail(),
                                                                request.getPassword()
                                                        ))
                                                )
                                                .onErrorResume(err ->
                                                        personService.compensateRegistration(personId.getId().toString())
                                                                .then(Mono.error(err))
                                                )
                                )
                )
                .map(tokenResponseMapper::toTokenResponse);
    }
}