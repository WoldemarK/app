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
@Table(name = "person.users")
public class User extends BaseEntity {

    private String email;
    private boolean filled;
    private String lastName;
    private String firstName;
    private String secretKey;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Address addressId;
}
