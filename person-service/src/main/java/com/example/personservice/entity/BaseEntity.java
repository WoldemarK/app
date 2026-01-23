package com.example.personservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ColumnDefault("true")
    @Column(nullable = false)
    private Boolean active;

    @NotNull
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(nullable = false)
    private Instant created;

    @NotNull
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(nullable = false)
    private Instant updated;

}