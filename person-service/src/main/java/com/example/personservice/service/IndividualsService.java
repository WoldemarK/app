package com.example.personservice.service;

import com.example.person.dto.IndividualDto;
import com.example.person.dto.IndividualWriteDto;
import com.example.person.dto.IndividualWriteResponseDto;
import com.example.personservice.entity.Individual;
import com.example.personservice.exception.PersonException;
import com.example.personservice.mapper.IndividualMapper;
import com.example.personservice.repository.IndividualRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualsService {

    private final IndividualMapper individualMapper;
    private final IndividualRepository individualRepository;

    @Transactional
    public UUID register(IndividualWriteDto writeDto) {
        Individual individual = individualMapper.to(writeDto);
        log.info("IN - register: individual: [{}] successfully registered", individual.getUser().getEmail());
        return individualRepository.save(individual).getId();
    }

    public List<IndividualDto> findByEmails(List<String> emails) {
        return individualRepository.findAllByEmails(emails)
                .stream()
                .map(individualMapper::from)
                .collect(Collectors.toList());
    }

    public IndividualDto findById(UUID id) {
        log.info("IN - findById: individual with id = [{}] successfully found", id);
        return individualRepository.findById(id)
                .map(individualMapper::from)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));


    }

    @Transactional
    public void softDelete(UUID id) {
        log.info("IN - softDelete: individual with id = [{}] successfully deleted", id);
        individualRepository.softDelete(id);
    }

    @Transactional
    public void hardDelete(UUID id) {
        Individual individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        log.info("IN - hardDelete: individual with id = [{}] successfully deleted", id);
        individualRepository.delete(individual);
    }

    @Transactional
    public IndividualWriteResponseDto update(UUID id, IndividualWriteDto writeDto) {
        Individual individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        individualMapper.update(individual, writeDto);
        individualRepository.save(individual);
        return new IndividualWriteResponseDto(individual.getId().toString());
    }

}
