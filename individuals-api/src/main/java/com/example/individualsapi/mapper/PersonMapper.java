package com.example.individualsapi.mapper;

import com.example.individuals.dto.IndividualDto;
import com.example.individuals.dto.IndividualWriteResponseDto;
import com.example.person.dto.IndividualWriteDto;
import org.mapstruct.Mapper;

import javax.validation.Valid;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface PersonMapper {


    IndividualDto from(IndividualDto dto);
    IndividualWriteResponseDto from(IndividualWriteResponseDto dto);

    @Valid IndividualWriteDto from(com.example.individuals.dto.IndividualWriteDto request);
}
