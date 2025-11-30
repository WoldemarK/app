package com.example.individualsapi.client;

import com.example.individuals.dto.TokenRefreshRequest;
import com.example.individuals.dto.TokenResponse;
import com.example.individuals.dto.UserLoginRequest;
import com.example.individualsapi.config.KeycloakProperties;
import com.example.individualsapi.dto.KeycloakCredentialsRepresentation;
import com.example.individualsapi.dto.KeycloakUserRepresentation;
import com.example.individualsapi.exception.AccessDeniedException;
import com.example.individualsapi.exception.BadRequestException;
import com.example.individualsapi.exception.ExternalServiceException;
import com.example.individualsapi.util.UserIdExtractor;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class KeycloakClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final WebClient webClient;
    private final KeycloakProperties props;

    private final String userByIdUrl;
    private final String userRegistrationUrl;
    private final String userPasswordResetUrl;

    public KeycloakClient(KeycloakProperties props, WebClient webClient) {
        this.props = props;
        this.webClient = webClient;
        this.userRegistrationUrl = "%s/admin/realms/%s/users".formatted(props.serverUrl(), props.realm());
        this.userByIdUrl = userRegistrationUrl + "/{id}";
        this.userPasswordResetUrl = userByIdUrl + "/reset-password";
    }

    @WithSpan("keycloakClient.login")
    public Mono<TokenResponse> login(UserLoginRequest req) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", props.clientId());
        form.add("username", req.getEmail());
        form.add("password", req.getPassword());
        addIfNotBlank(form, "client_secret", props.clientSecret());
        return requestToken(form);
    }

    @WithSpan("keycloakClient.adminLogin")
    public Mono<TokenResponse> adminLogin() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", props.adminClientId());
        form.add("username", props.adminUsername());
        form.add("password", props.adminPassword());
        return requestToken(form);
    }

    @WithSpan("keycloakClient.refreshToken")
    public Mono<TokenResponse> refreshToken(TokenRefreshRequest req) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", props.clientId());
        form.add("refresh_token", req.getRefreshToken());
        addIfNotBlank(form, "client_secret", props.clientSecret());
        return requestToken(form);
    }

    @WithSpan("keycloakClient.registerUser")
    public Mono<String> registerUser(TokenResponse adminToken, KeycloakUserRepresentation user) {
        return webClient.post()
                .uri(userRegistrationUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchangeToMono(this::extractIdFromLocationHeader);
    }

    @WithSpan("keycloakClient.resetUserPassword")
    public Mono<Void> resetUserPassword(String userId, KeycloakCredentialsRepresentation dto, String adminAccessToken) {
        return webClient.put()
                .uri(userPasswordResetUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .toBodilessEntity()
                .then();
    }

    private Mono<TokenResponse> requestToken(MultiValueMap<String, String> formData) {
        return webClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(TokenResponse.class);
    }

    private Mono<String> extractIdFromLocationHeader(ClientResponse response) {
        if (response.statusCode() == HttpStatus.CREATED) {
            URI location = response.headers().asHttpHeaders().getLocation();
            if (location == null) {
                return Mono.error(new BadRequestException("Location header missing in Keycloak user creation response"));
            }
            String path = location.getPath();
            if (path == null || path.isEmpty()) {
                return Mono.error(new BadRequestException("Location header contains empty path"));
            }
            return Mono.justOrEmpty(UserIdExtractor.extractIdFromPath(path))
                    .switchIfEmpty(Mono.error(new BadRequestException("Could not extract user ID from Location path: " + path)));
        }

        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new BadRequestException("Keycloak user creation failed: " + body)));
    }

    private Mono<? extends Throwable> toApiException(ClientResponse response) {
        HttpStatusCode statusCode = response.statusCode();
        return response.bodyToMono(String.class)
                .defaultIfEmpty("No error body")
                .map(body -> {
                    String message = "Keycloak error %s: %s".formatted(statusCode, body);
                    if (statusCode.equals(HttpStatus.UNAUTHORIZED) || statusCode.equals(HttpStatus.FORBIDDEN)) {
                        return new AccessDeniedException(message);
                    } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return new NotFoundException(message);
                    } else if (statusCode.is4xxClientError()) {
                        return new BadRequestException(message);
                    } else {
                        return new ExternalServiceException(message);
                    }
                });
    }

    private static void addIfNotBlank(MultiValueMap<String, String> form, String key, String value) {
        if (value != null && !value.isBlank()) {
            form.add(key, value);
        }
    }

}
