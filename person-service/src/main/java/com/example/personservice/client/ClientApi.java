package com.example.personservice.client;

import com.example.personservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "individuals-api", url = "${auth.service.base-url}")
public interface ClientApi {

    @PostMapping("/api/v1/auth/registration")
    void create(@RequestBody UserDto userDto);
}
