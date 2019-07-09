[![Build Status](https://travis-ci.org/ufukhalis/rxdb.svg?branch=master)](https://travis-ci.org/ufukhalis/rxdb)
[![Coverage Status](https://coveralls.io/repos/github/ufukhalis/rxdb/badge.svg?branch=master)](https://coveralls.io/github/ufukhalis/rxdb?branch=master)
![Maven Central](https://img.shields.io/maven-central/v/io.github.ufukhalis/rxdb.svg)
[![Join the chat at https://gitter.im/sahat/hackathon-starter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/rxdb-community/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

![Alt text](rxdb-logo-mini.png?raw=true "RXDB")

RXDB
===================
A reactive library option for JDBC calls. In the background, it uses Reactor library (https://projectreactor.io).

How to Use
------------
Firstly, you should add latest `rxdb` dependency to your project.

    <dependency>
        <groupId>io.github.ufukhalis</groupId>
        <artifactId>rxdb</artifactId>
        <version>0.1.0</version>
    </dependency>
    
Then you need to add jdbc driver for your database which you want to connect.

After, adding dependencies, you can create an instance from `Database` class.

    Database database = new Database.Builder()
            .maxConnections(5) // Default 10
            .minConnections(2) // Default 5
            .periodForHealthCheck(Duration.ofSeconds(5)) // Default 5 seconds
            .jdbcUrl("jdbc:h2:~/test") // In-memory db
            .healthCheck(HealthCheck.H2) // Default HealthCheck.OTHER
            .build();
            
Using `Database` instance, you can send queries to your database.

Inserting a record to the table.

    final String insertSql = "INSERT INTO table_name VALUES (1, 'Ufuk', 'Halis', 28)";
    Mono<Integer> resultMono = database.executeUpdate(insertSql);

Or you can follow below approach.
    
    final String insertSql = "INSERT INTO table_name VALUES (?, ?, ?, ?)";
    Mono<Integer> resultMono = database.update(insertSql)
            .bindParameters(1, "Ufuk", "Halis", 28)
            .get();

    
Fetching records from the table.

    final String selectSql = "select * from table_name where id=1";
    Flux<ResultSet> resultFlux = database.executeQuery(selectSql);

Or you can follow below approach.
    
    final String selectSql = "select * from table_name where id=?";
    Flux<ResultSet> resultFlux = database.select(selectSql);
            .bindParameters(1)
            .get();
   

You can also map your records directly to Java Object too.
Firstly, you should add `@Column` annotation to your pojo class like below.
        
    public class TestEntity {
        @Column(value = "id")
        private int id;
    
        @Column(value = "first")
        private String first;
    
        @Column(value = "last")
        private String last;
    
        @Column(value = "age")
        private int age;
        
        // Getters and Setters
    }
    
After, you can use your `database` instance like below.

    Flux<TestEntity> result = database
                    .select(selectSql)
                    .get(TestEntity.class);
                    
Also, if you would like to get only one result, you can use `findFirst` method.
    
    Mono<TestEntity> result = database
                    .findFirst(TestEntity.class);

Note
---

This project is still under development. But you can use in your projects.

For more information about Project Reactor, check the site https://projectreactor.io

For more information about vavr.io, check the site http://vavr-io.github.io

License
---
All code in this repository is licensed under the Apache License, Version 2.0. See [LICENCE](./LICENSE).