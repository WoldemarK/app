package com.example.personservice.repository;

import com.example.personservice.entity.Countries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CountryRepository extends JpaRepository<Countries, Integer> {

}
