package io.github.ufukhalis.query;

import io.github.ufukhalis.Predicates;
import io.github.ufukhalis.Column;
import io.vavr.collection.List;
import io.vavr.control.Try;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.ResultSet;
import java.util.function.Function;

import static io.vavr.API.*;

public class Select extends QueryParameter<Select> {

    private Function<String, Flux<ResultSet>> queryFunc;

    public Select(String sql, Function<String, Flux<ResultSet>> queryFunc) {
        super(sql);
        this.queryFunc = queryFunc;
    }

    public Flux<ResultSet> get() {
        return queryFunc.apply(getBindedSql());
    }

    public <T> Flux<T> get(Class<T> clazz) {
        return queryFunc.apply(getBindedSql())
                .map(resultSet ->
                        Try.of(() -> find(resultSet, clazz))
                                .getOrElseThrow(e -> new RuntimeException("Error", e))
        );
    }

    public <T> Mono<T> findFirst(Class<T> clazz) {
        return get(clazz).collectList()
                .flatMap(resultList ->
                        Try.of(() -> Mono.just(resultList.get(0)))
                                .getOrElse(Mono.empty())
                );
    }

    private <T> T find(ResultSet resultSet, Class<T> clazz) throws Exception {
        final T entity = clazz.getConstructor().newInstance();

        List.of(clazz.getDeclaredFields())
                .filter(field -> !field.getName().contains("jacoco"))
                .forEach(field -> {
                    Column column = field.getAnnotation(Column.class);
                    final Object object = getValueFromResultSet(field.getType(), column.value(), resultSet);
                    field.setAccessible(true);
                    Try.run(() -> field.set(entity, object))
                            .getOrElseThrow(e -> new RuntimeException("Field is not accessible", e));
                });

        return entity;
    }

    public String getSql() {
        return getBindedSql();
    }

    private Object getValueFromResultSet(Class<?> fieldClass, String columnName, ResultSet rs) {
        return Match(fieldClass).of(
                Case($(Predicates.isInstanceOfString), () -> Try.of(() -> rs.getString(columnName)).getOrElseThrow(e -> new RuntimeException("String casting error", e))),
                Case($(Predicates.isInstanceOfInteger),() ->  Try.of(() -> rs.getInt(columnName)).getOrElseThrow(e -> new RuntimeException("Integer casting error", e))),
                Case($(Predicates.isInstanceOfLong), () ->  Try.of(() -> rs.getLong(columnName)).getOrElseThrow(e -> new RuntimeException("Long casting error", e))),
                Case($(Predicates.isInstanceOfDouble), () ->  Try.of(() -> rs.getDouble(columnName)).getOrElseThrow(e -> new RuntimeException("Double casting error", e))),
                Case($(Predicates.isInstanceOfFloat), () ->  Try.of(() -> rs.getFloat(columnName)).getOrElseThrow(e -> new RuntimeException("Float casting error", e))),
                Case($(Predicates.isInstanceOfBoolean), () ->  Try.of(() -> rs.getBoolean(columnName)).getOrElseThrow(e -> new RuntimeException("Boolean casting error", e))),
                Case($(), () ->  new RuntimeException("Class type couldn't found"))
        );
    }
}
