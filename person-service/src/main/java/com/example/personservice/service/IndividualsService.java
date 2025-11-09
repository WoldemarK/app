package com.example.personservice.service;

import com.example.personservice.repository.IndividualsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualsService {

    private final IndividualsRepository individualsRepository;
}
