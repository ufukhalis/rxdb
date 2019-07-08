package io.github.ufukhalis.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class UpdateTests {

    @Test
    void test_bindParameters_shouldReturn_correctSql() {
        Update update = new Update("update table set value=?, value2=? where id=?", sql -> Mono.empty());
        String sql = update.bindParameters(1L, true, "id").getBindedSql();

        Assertions.assertEquals("update table set value=1, value2=true where id='id'", sql);
    }

    @Test
    void test_get_shouldReturn_validValue() {
        Update update = new Update("update table set value=?, value2=? where id=?", sql -> Mono.just(1));

        Assertions.assertEquals(1, update.get().block().intValue());
    }
}
