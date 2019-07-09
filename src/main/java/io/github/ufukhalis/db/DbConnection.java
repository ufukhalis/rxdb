package io.github.ufukhalis.db;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnection {

    private static final Logger log = LoggerFactory.getLogger(DbConnection.class);

    private DbConnection() {

    }

    static Mono<Connection> createFromMono(@NonNull String jdbcUrl) {
        return Mono.fromCallable(() -> createFrom(jdbcUrl, true));
    }

    static Mono<Connection> createFromMono(@NonNull String jdbcUrl, boolean autoCommit) {
        return Mono.fromCallable(() -> createFrom(jdbcUrl, autoCommit));
    }

    private static Connection createFrom(@NonNull String jdbcUrl, boolean autoCommit) {
        return Try.of(() -> {
            log.debug("Creating connection via jdbc url {}", jdbcUrl);
            final Connection connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(autoCommit);
            return connection;
        }).getOrElseThrow(() -> new RuntimeException("Connection couldn't created"));
    }

}
