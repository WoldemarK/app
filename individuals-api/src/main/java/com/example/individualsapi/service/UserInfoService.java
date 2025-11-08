package com.example.individualsapi.service;
import com.example.individuals_api.api.dto.AllUserInfirmationSystem;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {

    public AllUserInfirmationSystem getMe(Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String username = jwt.getClaim("preferred_username");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");
        return new AllUserInfirmationSystem()
                .id(userId)
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName);
    }
}
