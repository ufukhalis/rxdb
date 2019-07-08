package io.github.ufukhalis;

import io.github.ufukhalis.db.HealthCheck;
import io.github.ufukhalis.model.TestEntity;
import io.vavr.control.Try;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

public class DatabaseTests {

    final String createTableSql =  "drop table if exists REGISTER; " +
            " CREATE TABLE REGISTER " +
            "(id INTEGER not NULL, " +
            " first VARCHAR(255), " +
            " last VARCHAR(255), " +
            " age INTEGER, " +
            " PRIMARY KEY ( id ))";

    final String insertSql = "INSERT INTO REGISTER " + "VALUES (100, 'Zara', 'Ali', 18)";
    final String insertSql2 = "INSERT INTO REGISTER " + "VALUES (101, 'Zaras', 'Aliz', 19)";

    final String selectSql = "select * from REGISTER";

    final Database database = new Database.Builder()
            .maxConnections(5)
            .minConnections(2)
            .periodForHealthCheck(Duration.ofSeconds(5))
            .jdbcUrl("jdbc:h2:~/test")
            .healthCheck(HealthCheck.H2)
            .build();

    @Test
    void test_executeUpdate_shouldReturn_validInteger() {
        StepVerifier.create(database.executeUpdate(createTableSql))
                .expectNextMatches(value -> value == 0)
                .verifyComplete();
    }

    @Test
    void test_executeQuery_shouldReturn_correctSize() {
        database.executeUpdate(createTableSql).block();
        database.executeUpdate(insertSql).block();
        database.executeUpdate(insertSql2).block();

        int size = database.executeQuery(selectSql)
                .map(resultSet -> Try.of(() -> resultSet.getInt("id"))
                        .getOrElseThrow(e -> new RuntimeException("Unexpected exception", e)))
                .collectList()
                .block()
                .size();

        Assertions.assertEquals(2, size);
    }

    @Test
    void test_select_get_shouldReturn_valid_listSize() {
        database.executeUpdate(createTableSql).block();
        database.executeUpdate(insertSql).block();
        database.executeUpdate(insertSql2).block();

        List<TestEntity> list = database
                .select(selectSql)
                .get(TestEntity.class)
                .collectList()
                .block();

        Assertions.assertEquals(2, list.size());
    }

    @Test
    void test_select_findFirst_shouldReturn_a_object() {
        database.executeUpdate(createTableSql).block();
        database.executeUpdate(insertSql).block();

        TestEntity testEntity = database.select(selectSql)
                .findFirst(TestEntity.class).block();

        Assertions.assertNotNull(testEntity);
    }
}
