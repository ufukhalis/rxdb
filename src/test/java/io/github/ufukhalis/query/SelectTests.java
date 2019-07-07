package io.github.ufukhalis.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class SelectTests {

    @Test
    void test_bindParameters_shouldReturn_correctSql() {
        Select select = new Select("select * from where id=? and uid=?", sql -> Flux.empty());
        String bindedSql = select.bindParameters(11, "123").getSql();

        Assertions.assertEquals("select * from where id=11 and uid='123'", bindedSql);
    }

}
