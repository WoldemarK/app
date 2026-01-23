package com.example.personservice.rest;

import com.example.person.api.PersonApi;
import com.example.person.dto.IndividualDto;
import com.example.person.dto.IndividualPageDto;
import com.example.person.dto.IndividualWriteDto;
import com.example.person.dto.IndividualWriteResponseDto;
import com.example.personservice.entity.Individual;
import com.example.personservice.service.IndividualsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Email;
import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class IndividualRestControllerV1 implements PersonApi {

    private final IndividualsService individualService;

    @Override
    public ResponseEntity<Void> compensateRegistration(UUID id) {
        individualService.hardDelete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        individualService.softDelete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<IndividualPageDto> findAllByEmail(List<@Email String> email) {
        return ResponseEntity.ok(individualService.findByEmails(email));
    }

    @Override
    public ResponseEntity<IndividualDto> findById(UUID id) {
        return ResponseEntity.ok(individualService.findById(id));
    }

    @Override
    public ResponseEntity<IndividualWriteResponseDto> registration(IndividualWriteDto individualWriteDto) {
        return ResponseEntity.ok(individualService.register(individualWriteDto));
    }

    @Override
    public ResponseEntity<IndividualWriteResponseDto> update(UUID id, IndividualWriteDto individualWriteDto) {
        return ResponseEntity.ok(individualService.update(id, individualWriteDto));
    }
}
