package com.example.individualsapi.mapper;

import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface PersonMapper {

    com.example.person.dto.IndividualWriteDto from
            (
                    com.example.individuals.dto.IndividualWriteDto dto
            );


    com.example.person.dto.IndividualDto from
            (
                    com.example.individuals.dto.IndividualDto dto)
            ;

    com.example.individuals.dto.IndividualDto from
            (
                    com.example.person.dto.IndividualDto dto)
            ;

    com.example.individuals.dto.IndividualWriteResponseDto from
            (
                    com.example.person.dto.IndividualWriteResponseDto dto
            );
}
