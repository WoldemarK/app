package com.example.individualsapi.dto;

import java.util.Map;

public record KeycloakUserRepresentation
        (
                String id,
                String username,
                String email,
                Boolean enabled,
                Boolean emailVerified,
                Map<String, String> attributes
        ) {
}
