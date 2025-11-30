package com.example.individualsapi.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class AuthoritiesConverter implements Converter<Jwt, Flux<GrantedAuthority>> {
    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Object realmAccess = jwt.getClaims().get("realm_access");

        if (realmAccess instanceof Map) {
            Object roles = ((Map<?, ?>) realmAccess).get("roles");
            if (roles instanceof Collection<?>) {
                ((Collection<?>) roles).stream()
                        .filter(role -> role instanceof String)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
            }
        }
        return Flux.fromIterable(authorities);
    }
}
