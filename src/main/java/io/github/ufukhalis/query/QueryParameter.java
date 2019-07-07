package io.github.ufukhalis.query;

import io.github.ufukhalis.Predicates;
import io.github.ufukhalis.Utils;
import io.vavr.collection.List;
import io.vavr.control.Option;

import static io.vavr.API.*;

class QueryParameter {

    private final static String BIND_PARAM_HOLDER = "\\?";

    private String bindedSql;

    String bind(final String sql, List<Object> params) {
        Utils.objectRequireNonNull(params, Option.some("Parameters cannot be null!s"));
        Utils.checkCondition(params.size() > 0, Option.some("Parameters cannot be empty!"));

        this.bindedSql = sql;

        params.forEach(o -> this.bindedSql = bindedSql.replaceFirst(BIND_PARAM_HOLDER, resolveTypeForSQLValue(o)));

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
