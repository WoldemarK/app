package com.example.individualsapi.dto;

public record KeycloakCredentialsRepresentation
        (
                String type,
                String value,
                Boolean temporary
        ){
}
