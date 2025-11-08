package com.example.individualsapi.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] permitAllPaths = {"/api/v1/auth/registration", "/api/v1/auth/login",
            "/api/test/log"};
    private static final String[] authenticatedPaths = {"/v1/auth/me", "/v1/auth/refresh-token"};

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(permitAllPaths).permitAll()
                                .requestMatchers(authenticatedPaths).authenticated()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2Resource ->
                        oauth2Resource
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))

                                .authenticationEntryPoint((request,
                                                           response,
                                                           authException) ->
                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                                )
                                .accessDeniedHandler((request,
                                                      response,
                                                      accessDeniedException) ->
                                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                                )
                ).sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new AuthoritiesConverter());
        return converter;
    }

}
