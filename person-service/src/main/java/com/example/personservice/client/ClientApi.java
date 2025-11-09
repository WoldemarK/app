package com.example.personservice.client;

import com.example.personservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "individuals-api", url = "${feignClientUrl}")
public interface ClientApi {

    @PostMapping
    UserDto create(@RequestBody UserDto userDto);
}
