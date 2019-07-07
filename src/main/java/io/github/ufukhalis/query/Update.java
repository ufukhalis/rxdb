package io.github.ufukhalis.query;

import reactor.core.publisher.Mono;

import java.util.function.Function;

public class Update extends QueryParameter<Update> {

    private Function<String, Mono<Integer>> queryFunc;

    public Update(String sql, Function<String, Mono<Integer>> queryFunc) {
        super(sql);
        this.queryFunc = queryFunc;
    }

    public Mono<Integer> get() {
        return queryFunc.apply(getBindedSql());
    }
}
