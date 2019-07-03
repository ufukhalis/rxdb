package io.github.ufukhalis.mapper;

import reactor.util.annotation.NonNull;

import java.sql.ResultSet;
import java.util.function.Function;

public interface ResultSetMapper<T> extends Function<ResultSet, T> {

    @Override
    T apply(@NonNull ResultSet rs);
}
