package com.example.individualsapi.service;

import com.example.individuals_api.api.dto.TokenResponse;
import com.example.individuals_api.api.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RestTemplate restTemplate;

    @Value("${TOKEN-URL}")
    private String tokenUrl;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    public ResponseEntity<TokenResponse> getToken(UserLoginRequest request) {
        log.info("Registering user: {}", request);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("username", request.getEmail());
        formData.add("password", request.getPassword());
        formData.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        return restTemplate.postForEntity(tokenUrl, requestEntity, TokenResponse.class);

    }
}
