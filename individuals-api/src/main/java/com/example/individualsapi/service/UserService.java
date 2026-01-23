package com.example.individualsapi.service;

import com.example.individual.dto.IndividualWriteDto;
import com.example.individual.dto.TokenResponse;
import com.example.individual.dto.UserInfoResponse;
import com.example.individualsapi.client.KeycloakClient;
import com.example.individualsapi.dto.KeycloakCredentialsRepresentation;
import com.example.individualsapi.dto.KeycloakUserRepresentation;
import com.example.individualsapi.exception.ApiException;
import com.example.individualsapi.mapper.TokenResponseMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PersonService personService;
    private final KeycloakClient keycloakClient;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan(value = "userService.getCurrentUserInfo")
    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(UserService::getUserInfoResponseMono)
                .switchIfEmpty(Mono.error(new ApiException("No authentication present")));
    }

    private static Mono<UserInfoResponse> getUserInfoResponseMono(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            UserInfoResponse userInfoResponse = new UserInfoResponse();
            userInfoResponse.setId(jwt.getSubject());
            userInfoResponse.setEmail(jwt.getClaimAsString("email"));
            userInfoResponse.setRoles(jwt.getClaimAsStringList("roles"));

            if (jwt.getIssuedAt() != null) {
                userInfoResponse.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            }
            log.info("User[email={}] was successfully get info", jwt.getClaimAsString("email"));

            return Mono.just(userInfoResponse);
        }

        log.error("Can not get current user info: Invalid principal");
        return Mono.error(new ApiException("Can not get current user info: Invalid principal"));
    }

    @WithSpan("userService.register")
    public Mono<TokenResponse> register(IndividualWriteDto request) {
        return personService.register(request)
                .flatMap(personId ->
                        keycloakClient.adminLogin()
                                .flatMap(adminToken -> {
                                    KeycloakUserRepresentation kcUser = new KeycloakUserRepresentation(
                                            null,
                                            request.getEmail(),
                                            request.getEmail(),
                                            true,
                                            true,
                                            null
                                    );
                                    return keycloakClient.registerUser(adminToken, kcUser)
                                            .flatMap(kcUserId -> {
                                                KeycloakCredentialsRepresentation cred =
                                                        new com.example.individualsapi.dto.KeycloakCredentialsRepresentation(
                                                        "password",
                                                        request.getPassword(),
                                                        false
                                                );
                                                return keycloakClient
                                                        .resetUserPassword(kcUserId, cred, adminToken.getAccessToken())
                                                        .thenReturn(kcUserId);
                                            })
                                            .flatMap(r ->
                                                    keycloakClient.login(
                                                            new com.example.keycloak.dto.UserLoginRequest(
                                                                    request.getEmail(),
                                                                    request.getPassword()
                                                            )
                                                    )
                                            )
                                            .onErrorResume(err ->
                                                    personService.compensateRegistration(personId.getId().toString())
                                                            .then(Mono.error(err))
                                            )
                                            .map(tokenResponseMapper::toTokenResponse);

                                })
                );
    }
}