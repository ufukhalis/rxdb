[![Build Status](https://travis-ci.org/ufukhalis/rxdb.svg?branch=master)](https://travis-ci.org/ufukhalis/rxdb)
[![Coverage Status](https://coveralls.io/repos/github/ufukhalis/rxdb/badge.svg?branch=master)](https://coveralls.io/github/ufukhalis/rxdb?branch=master)
![Maven Central](https://img.shields.io/maven-central/v/io.github.ufukhalis/rxdb.svg)

RXDB
===================
A reactive library option for JDBC calls. In the background, it uses Reactor library (https://projectreactor.io).

How to Use
------------
Firstly, you should add latest `rxdb` dependency to your project.

    <dependency>
        <groupId>io.github.ufukhalis</groupId>
        <artifactId>rxdb</artifactId>
        <version>0.0.1</version>
    </dependency>
    
Then you need to add jdbc driver for your database which you want to connect.

After, adding dependencies, you can create an instance from `Database` class.

    Database database = new Database.Builder()
            .maxConnections(5) // Default 10
            .minConnections(2) // Default 5
            .periodForHealthCheckInMillis(5000) // 5000
            .jdbcUrl("jdbc:h2:~/test") // In-memory db
            .healthCheck(HealthCheck.H2) // Default HealthCheck.OTHER
            .build();
            
Using `Database` instance, you can send queries to your database.

Inserting a record to the table.

    final String insertSql = "INSERT INTO table_name VALUES (1, 'Ufuk', 'Halis', 28)";
    Mono<Integer> resultMono = database.executeUpdate(insertSql);
    
Fetching records from the table.

    final String selectSql = "select * from table_name";
    Flux<ResultSet> resultFlux = database.executeQuery(selectSql);

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