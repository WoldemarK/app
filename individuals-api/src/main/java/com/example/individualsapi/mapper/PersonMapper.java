package com.example.individualsapi.mapper;

import com.example.individuals.dto.IndividualDto;
import com.example.individuals.dto.IndividualWriteDto;
import com.example.individuals.dto.IndividualWriteResponseDto;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface PersonMapper {

    IndividualWriteDto from(IndividualWriteDto dto);
    IndividualDto from(IndividualDto dto);
    IndividualWriteResponseDto from(IndividualWriteResponseDto dto);
}
