package io.github.ufukhalis;

import io.github.ufukhalis.db.ConnectionPool;
import io.github.ufukhalis.db.HealthCheck;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class Database {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private final ConnectionPool connectionPool;

    private final long periodForHealthCheckInMillis;
    private final HealthCheck healthCheck;

    private Database (int maxConnections,
                      int minConnections,
                      String jdbcUrl,
                      HealthCheck healthCheck,
                      long periodForHealthCheckInMillis) {
        this.periodForHealthCheckInMillis = periodForHealthCheckInMillis;
        this.healthCheck = healthCheck;
        this.connectionPool =
                new ConnectionPool(
                        maxConnections,
                        minConnections,
                        jdbcUrl,
                        Executors.newFixedThreadPool(maxConnections * 2)
                );

        createIntervalForHealthChecking();
    }

    public Mono<ResultSet> executeQuery(String sql) {
        log.debug("Executing query -> {}", sql);

        Mono<Connection> connectionMono = this.connectionPool.getConnection();

        return connectionMono.map(connection ->
            Try.of(() -> connection.prepareStatement(sql))
                    .map(preparedStatement -> Try.of(preparedStatement::executeQuery)
                            .getOrElseThrow(e -> new RuntimeException("Query execution failed", e))
                    ).getOrElseThrow(e -> new RuntimeException("Prepare statement failed", e))
        ).doAfterSuccessOrError((rs, throwable) -> connectionPool.releaseConnection(connectionMono).subscribe());
    }

    public Mono<Integer> executeUpdate(String sql) {
        log.debug("Executing query -> {}", sql);

        Mono<Connection> connectionMono = this.connectionPool.getConnection();

        return connectionMono.map(connection ->
                Try.withResources(connection::createStatement)
                        .of(statement -> statement.executeUpdate(sql))
                        .getOrElseThrow(e -> new RuntimeException("Query execution failed", e))
        ).doAfterSuccessOrError((rs, throwable) -> connectionPool.releaseConnection(connectionMono).subscribe());
    }

    private void createIntervalForHealthChecking() {
        Flux.interval(Duration.ofMillis(this.periodForHealthCheckInMillis))
                .doOnEach(ignore -> {
                    log.debug("Health checking...");
                    executeQuery(healthCheck.getSql()).subscribe();
                })
                .subscribeOn(Schedulers.single())
                .subscribe();
    }

    public final static class Builder {
        private int maxConnections = 10;
        private int minConnections = 5;
        private long periodForHealthCheckInMillis = 5000;
        private String jdbcUrl;
        private HealthCheck healthCheck = HealthCheck.OTHER;

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder minConnections(int minConnections) {
            this.minConnections = minConnections;
            return this;
        }

        public Builder periodForHealthCheckInMillis(int millis) {
            this.periodForHealthCheckInMillis = millis;
            return this;
        }

        public Builder jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder healthCheck(HealthCheck healthCheck) {
            this.healthCheck = healthCheck;
            return this;
        }

        public Database build() {
            return new Database(
                    this.maxConnections,
                    this.minConnections,
                    this.jdbcUrl,
                    this.healthCheck,
                    this.periodForHealthCheckInMillis
            );
        }
    }
}
