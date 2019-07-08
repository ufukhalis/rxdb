package io.github.ufukhalis.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class Update extends QueryParameter<Update> {

    private static final Logger log = LoggerFactory.getLogger(Update.class);

    private Function<String, Mono<Integer>> queryFunc;

    public Update(String sql, Function<String, Mono<Integer>> queryFunc) {
        super(sql);
        this.queryFunc = queryFunc;
    }

    public Mono<Integer> get() {
        log.debug("Getting result..");
        return queryFunc.apply(getBindedSql());
    }
}
