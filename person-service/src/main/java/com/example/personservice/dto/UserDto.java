package com.example.personservice.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserDto
        (
                UUID uuid,
                String email,
                String lastName,
                String firstName,
                String password,
                String username
        ) {
}
