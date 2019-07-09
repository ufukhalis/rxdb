package io.github.ufukhalis.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class TransactionalUpdateTests {

    @Test
    void test_bindParameters_shouldReturn_correctElementSize() {
        TransactionalUpdate transactionalUpdate =
                new TransactionalUpdate("insert table values(?,?)", queries -> Mono.empty());

        String[] queries = transactionalUpdate
                .bindParameters(1, "ufuk", 2, "halis", 3, "Test")
                .getQueries();

        Assertions.assertEquals(3, queries.length);
    }
}
