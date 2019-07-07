package io.github.ufukhalis.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class UpdateTests {

    @Test
    public void test_bindParameters_shouldReturn_correctSql() {
        Update update = new Update("update table set value=?, value2=? where id=?", sql -> Mono.empty());
        String sql = update.bindParameters(1L, true, "id").getBindedSql();

        Assertions.assertEquals("update table set value=1, value2=true where id='id'", sql);
    }
}
