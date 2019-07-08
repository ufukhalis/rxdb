package io.github.ufukhalis.query;

import io.github.ufukhalis.Predicates;
import io.github.ufukhalis.Utils;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vavr.API.*;

class QueryParameter <T> {

    private static final Logger log = LoggerFactory.getLogger(QueryParameter.class);
    private final static String BIND_PARAM_HOLDER = "\\?";

    private String bindedSql;

    QueryParameter(String sql) {
        this.bindedSql = sql;
    }

    public T bindParameters(Object ...params) {
        this.bindedSql = bind(this.bindedSql, List.of(params));
        return (T) this;
    }

    String bind(final String sql, List<Object> params) {
        Utils.objectRequireNonNull(params, Option.some("Parameters cannot be null!s"));
        Utils.checkCondition(params.size() > 0, Option.some("Parameters cannot be empty!"));

        log.debug("Binding parameters..");
        log.debug("Sql {}, parameters {}", sql, params.mkString());

        this.bindedSql = sql;

        params.forEach(o -> this.bindedSql = bindedSql.replaceFirst(BIND_PARAM_HOLDER, resolveTypeForSQLValue(o)));

        return bindedSql;
    }

    String getBindedSql() {
        return bindedSql;
    }

    private static String resolveTypeForSQLValue(Object object) {
        final Class<?> objectClass = object.getClass();

        return Match(objectClass).of(
                Case($(Predicates.isInstanceOfString), () -> "'"  + Utils.escapeSql(object.toString()) + "'"),
                Case($(), () -> object + "")
        );
    }
}
