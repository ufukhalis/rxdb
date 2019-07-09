package io.github.ufukhalis.query;

import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class TransactionalUpdate extends QueryParameter<TransactionalUpdate> {

    private static final Logger log = LoggerFactory.getLogger(TransactionalUpdate.class);

    private Function<String[], Mono<Boolean>> queryFunc;
    private String[] queries;
    private String sql;

    public TransactionalUpdate(String query, Function<String[], Mono<Boolean>> queryFunc) {
        super(query);
        this.sql = query;
        this.queryFunc = queryFunc;
    }

    public Mono<Boolean> get() {
        log.debug("Getting result..");
        return queryFunc.apply(this.queries);
    }

    @Override
    public TransactionalUpdate bindParameters(Object... params) {
        int divider = getParameterDivider(params);

        log.debug("Binding transactional parameters..");

        this.queries = List.of(params)
                .grouped(divider)
                .map(groupedParams -> bind(sql, groupedParams))
                .toJavaList().toArray(new String[divider / params.length]);
        return this;
    }

    private int getParameterDivider(Object... params) {
        String query = this.sql;
        int queryParamCount = List.ofAll(query.toCharArray()).count(ch -> ch == '?');
        int paramCount = params.length;
        if (paramCount % queryParamCount != 0) {
            throw new RuntimeException("You should set all parameters!");
        }
        return queryParamCount;
    }

    public String[] getQueries() {
        return queries;
    }
}
