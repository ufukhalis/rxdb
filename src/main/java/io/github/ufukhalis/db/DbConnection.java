package io.github.ufukhalis.db;

import io.vavr.control.Try;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnection {

    private DbConnection() {

    }

    static Mono<Connection> createFromMono(@NonNull String jdbcUrl) {
        return Mono.fromCallable(() -> createFrom(jdbcUrl));
    }

    private static Connection createFrom(@NonNull String jdbcUrl) {
        return Try.of(() -> {
            final Connection connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(true);
            return connection;
        }).getOrElseThrow(() -> new RuntimeException("Connection couldn't created"));
    }

}
