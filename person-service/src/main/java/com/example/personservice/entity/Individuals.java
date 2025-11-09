package com.example.personservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Audited
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "person.individuals")
public class Individuals extends BaseEntity{

    private String status;
    private String phoneNumber;
    private String passportNumber;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private User userId;


}
