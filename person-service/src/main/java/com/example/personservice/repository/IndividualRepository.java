package com.example.personservice.repository;

import com.example.personservice.entity.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IndividualRepository extends JpaRepository<Individual, UUID> {

    @Query("FROM Individual i WHERE (:emails) IS NULL OR i.user.email IN :emails")
    List<Individual> findAllByEmails(@Param("emails") List<String> emails);

    @Modifying
    @Query("UPDATE Individual i SET i.active = false WHERE i.id = :id")
    void softDelete(@Param("id") UUID id);
}
