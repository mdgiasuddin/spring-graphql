package com.example.springgraphql.controller;

import com.example.springgraphql.model.Person;
import com.example.springgraphql.repository.PersonRepository;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonRepository personRepository;

    @Value("classpath:person.graphqls")
    private Resource schemaResource;

    private GraphQL graphQL;

    @PostConstruct
    public void loadSchema() throws IOException {
        File schemaFile = schemaResource.getFile();
        TypeDefinitionRegistry registry = new SchemaParser().parse(schemaFile);

        RuntimeWiring wiring = buildWiring();

        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    private RuntimeWiring buildWiring() {
        DataFetcher<List<Person>> allPersonFetcher = data -> (List<Person>) personRepository.findAll();
        DataFetcher<Person> personFetcher = data -> (Person) personRepository.findOneByEmail(data.getArgument("email"));

        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWriting -> typeWriting
                        .dataFetcher("getAllPerson", allPersonFetcher)
                        .dataFetcher("findPersonByEmail", personFetcher))
                .build();
    }

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }


    @PostMapping
    public String addPerson(@RequestBody List<Person> people) {
        personRepository.saveAll(people);

        return "Person saved successfully!";
    }

    @GetMapping
    public List<Person> getPeople() {
        return (List<Person>) personRepository.findAll();
    }

    @PostMapping("/all/graphql")
    public ResponseEntity<Object> geAllPerson(@RequestBody String query) {
        ExecutionResult result = graphQL.execute(query);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/email/graphql")
    public ResponseEntity<Object> gePersonByEmail(@RequestBody String query) {
        ExecutionResult result = graphQL.execute(query);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
