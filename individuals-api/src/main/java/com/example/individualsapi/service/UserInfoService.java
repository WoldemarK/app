package com.example.individualsapi.service;

import com.example.individuals_api.api.dto.AllUserInfirmationSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserInfoService {


    public AllUserInfirmationSystem getUserInformation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UsernameNotFoundException("Invalid username or password");
        }
        return getUserInfoResponse(authentication);
    }

    private static AllUserInfirmationSystem getUserInfoResponse(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {

            AllUserInfirmationSystem userInfo = new AllUserInfirmationSystem();
            userInfo.id(jwt.getSubject());
            userInfo.email(jwt.getClaim("email"));
            userInfo.username(jwt.getClaim("preferred_username"));
            userInfo.firstName(jwt.getClaim("given_name"));
            userInfo.lastName(jwt.getClaim("family_name"));

            log.info("User[email={}] was successfully get info", jwt.getClaimAsString("email"));
            return userInfo;
        }
        log.error("Invalid username or password");
        throw new UsernameNotFoundException("Invalid username or password");
    }
}
