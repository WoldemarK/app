package com.example.individualsapi.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin.admin-password}")
    private String adminPassword;

    @Value("${keycloak-realm-admin}")
    private String masterRealm;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .realm(masterRealm)
                .clientId(clientId)
                .serverUrl(serverUrl)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }
}
