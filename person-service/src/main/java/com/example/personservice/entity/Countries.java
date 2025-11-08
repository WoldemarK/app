package com.example.personservice.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "person.countries")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Countries extends BaseEntity {

    private String name;
    private String alpha2;
    private String alpha3;
    private String status;
}
