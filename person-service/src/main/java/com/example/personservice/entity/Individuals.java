package com.example.personservice.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "person.individuals")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Individuals extends BaseEntity{

    private String status;
    private String phoneNumber;
    private String passportNumber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User userId;


}
