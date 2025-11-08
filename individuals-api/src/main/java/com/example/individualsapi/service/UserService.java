package com.example.individualsapi.service;

import com.example.individuals_api.api.dto.TokenResponse;
import com.example.individuals_api.api.dto.UserLoginRequest;
import com.example.individuals_api.api.dto.UserRegistrationRequest;
import com.example.individualsapi.exception.BadRequestException;
import com.example.individualsapi.exception.ResponseException;
import com.example.individualsapi.exception.UserAlreadyExistsException;
import com.example.individualsapi.exception.ValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final Keycloak keycloak;
    private final TokenService tokenService;

    @Value("${keycloak-realm-name}")
    private String realm;

    @Value("${keycloak-default-role}")
    private String defaultRole;

    public TokenResponse registerUser(UserRegistrationRequest registration) {
        // Валидация входных данных
        validateCreatedNewUser(registration);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource userResource = realmResource.users();

        // Поиск по email если пользователь есть в системе то не даст зарегистрироваться повторно
        if (!userResource.searchByEmail(registration.getEmail(), true).isEmpty()) {
            throw new UserAlreadyExistsException(registration.getEmail());
        }

        // Создание нового пользователя
        UserRepresentation newUser = buildUserRepresentation(registration);
        Response response = userResource.create(newUser);

        try (response) {
            if (response.getStatus() != 201) {
                String body = safeReadResponse(response);
                throw new RuntimeException("Keycloak error: " + response.getStatus() + " - " + body);
            }
            String userId = userIdFromLocation(response.getLocation());
            assignDefaultRole(realmResource, userResource, userId, defaultRole);
            log.info("User '{}' (ID: {}) successfully created.", registration.getUsername(), userId);
        }
        return getAccessTokenAndRefreshToke(new UserLoginRequest(registration.getEmail(),
                registration.getPassword()));
    }

    private String safeReadResponse(Response response) {
        if (response == null) {
            return "Null response";
        }
        try {
            if (!response.hasEntity()) {
                return "No entity in response";
            }
            return response.readEntity(String.class);
        } catch (ResponseException e) {
            log.warn("Failed to read response body from Keycloak", e);
            return "Unreadable response body: " + e.getMessage();
        }
    }

    private static void validateCreatedNewUser(UserRegistrationRequest registration) {
        if (registration.getEmail() == null || registration.getEmail().trim().isEmpty()) {
            log.error("Registration failed: email is required");
            throw new ValidationError("Email is required");
        }
        if (registration.getUsername() == null || registration.getUsername().trim().isEmpty()) {
            log.error("Registration failed: username is required");
            throw new ValidationError("Username is required");
        }
        if (registration.getPassword() == null || registration.getPassword().length() < 8) {
            log.error("Registration failed: password is too weak or missing");
            throw new ValidationError("Password must be at least 8 characters");
        }
        if (registration.getLastName() == null || registration.getLastName().isEmpty()) {
            log.error("Registration failed: lastName is required");
        }
        if (registration.getFirstName() == null || registration.getFirstName().isEmpty()) {
            log.error("Registration failed: firstName is required");
        }
    }

    private void assignDefaultRole(RealmResource realm, UsersResource resource, String userId, String roleName) {
        try {
            RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
            resource.get(userId).roles().realmLevel().add(Collections.singletonList(role));
            log.info("Assigned role '{}' to user id '{}'", roleName, userId);
        } catch (NotFoundException e) {
            log.warn("Role '{}' not found. Skipping role assignment for user id '{}'.", roleName, userId);
        } catch (Exception e) {
            log.error("Failed to assign role '{}' to user '{}'", roleName, userId, e);
        }
    }

    private String userIdFromLocation(URI location) {
        if (location == null) {
            throw new IllegalArgumentException("Location URI must not be null");
        }

        String path = location.getPath();
        if (path == null || path.isEmpty()) {
            throw new IllegalStateException("Location path is null or empty: " + location);
        }

        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == path.length() - 1) {
            throw new IllegalStateException("No user ID in Location path: " + location);
        }

        String userId = path.substring(lastSlashIndex + 1);
        if (userId.isEmpty()) {
            throw new IllegalStateException("Extracted user ID is empty from: " + location);
        }

        return userId;
    }

    private UserRepresentation buildUserRepresentation(UserRegistrationRequest registration) {
        UserRepresentation user = new UserRepresentation();
        user.setEmail(registration.getEmail());
        user.setUsername(registration.getUsername());
        user.setFirstName(registration.getFirstName());
        user.setLastName(registration.getLastName());

        user.setEnabled(true);
        user.setEmailVerified(false);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registration.getPassword());
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        return user;
    }

    private TokenResponse getAccessTokenAndRefreshToke(UserLoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()) {
            throw new ValidationError("Email is required");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            throw new ValidationError("Password is required");
        }
        ResponseEntity<TokenResponse> response = tokenService.getToken(loginRequest);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Authentication failed: " + response.getStatusCode());
        }
        return response.getBody();
    }

    public String login(UserLoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            log.error("Требуется адрес электронной почты или пароль: {} {}", request.getEmail(), request.getPassword());
            throw new ValidationError("Требуется адрес электронной почты и пароль");
        }
        try {
            ResponseEntity<TokenResponse> response = tokenService.getToken(request);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Привет, аутентифицированный пользователь " + request.getEmail();
            } else {
                log.warn("Authentication failed for email: {}", request.getEmail());
                return "Неверные данные или пользователь не существует";
            }
        } catch (BadRequestException e) {
            log.warn("Authentication failed for email: {}", request.getEmail(), e);
            return "Неверные данные или пользователь не существует";
        }
    }
}
