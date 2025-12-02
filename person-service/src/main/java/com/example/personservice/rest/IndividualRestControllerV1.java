package com.example.personservice.rest;

import com.example.person.dto.IndividualWriteDto;
import com.example.personservice.service.IndividualsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/persons")
public class IndividualRestControllerV1 {

    private final IndividualsService individualsService;

    @PostMapping
    public ResponseEntity<UUID> save(@RequestBody IndividualWriteDto writeDto) {
        return ResponseEntity.ok(individualsService.register(writeDto));
    }

}
