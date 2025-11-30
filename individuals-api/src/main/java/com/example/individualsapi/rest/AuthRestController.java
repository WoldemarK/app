package com.example.individualsapi.rest;

import com.example.individuals.dto.UserInfoResponse;
import com.example.individualsapi.service.UserInfoService;
import com.example.individualsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final UserService userService;
    private final UserInfoService userInfoService;



    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        return ResponseEntity.ok(userInfoService.getUserInformation());
    }

}
