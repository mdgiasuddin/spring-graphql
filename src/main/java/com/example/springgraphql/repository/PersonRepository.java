package com.example.springgraphql.repository;

import com.example.springgraphql.model.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Integer> {

    Person findOneByEmail(String email);
}
