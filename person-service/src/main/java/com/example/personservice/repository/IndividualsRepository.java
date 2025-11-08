package com.example.personservice.repository;

import com.example.personservice.entity.Individuals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IndividualsRepository extends JpaRepository<Individuals, UUID> {
}
