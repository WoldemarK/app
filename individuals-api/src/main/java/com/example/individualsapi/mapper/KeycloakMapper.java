package com.example.individualsapi.mapper;

import com.example.individual.dto.TokenRefreshRequest;
import com.example.individual.dto.UserLoginRequest;
import com.example.individual.dto.UserLoginRequest;
import org.mapstruct.*;

import java.util.Map;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public abstract class KeycloakMapper {

    @Named("toAttributes")
    public Map<String, String> toAttributes(@Context String personId) {
        return Map.of("personId", personId);
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    public abstract com.example.keycloak.dto.UserLoginRequest toKeycloakUserLoginRequest(UserLoginRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "refreshToken", source = "refreshToken")
    public abstract com.example.keycloak.dto.TokenRefreshRequest toKeycloakTokenRefreshRequest(TokenRefreshRequest request);
}
