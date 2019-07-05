package io.github.ufukhalis;

import io.github.ufukhalis.db.ConnectionPool;
import io.github.ufukhalis.db.HealthCheck;
import io.github.ufukhalis.query.Select;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Duration;
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

    public Select select(String sql) {
        return new Select(executeQuery(sql));
    }

    public Flux<ResultSet> executeQuery(String sql) {
        Utils.objectRequireNonNull(sql, Option.some("Sql query cannot be empty!"));

        log.debug("Executing query -> {}", sql);

        Mono<Connection> connectionMono = this.connectionPool.getConnection();

        Mono<ResultSet> resultSetMono = connectionMono.map(connection -> {
            log.debug("Executing query -> {}", sql);
            return  Try.of(() -> connection.prepareStatement(sql))
                    .map(preparedStatement -> Try.of(preparedStatement::executeQuery)
                            .getOrElseThrow(e -> new RuntimeException("Query execution failed", e))
                    ).getOrElseThrow(e -> new RuntimeException("Prepare statement failed", e));
        }).doFinally(ignore -> this.connectionPool.add(connectionMono));

        return resultSetMono.flatMapMany(Utils::convertMonoResultSetToFlux);
    }


    public Mono<Integer> executeUpdate(String sql) {
        Utils.objectRequireNonNull(sql, Option.some("Sql query cannot be empty!"));

        Mono<Connection> connectionMono = this.connectionPool.getConnection();

        return connectionMono.map(connection -> {
            log.debug("Executing query -> {}", sql);
            return Try.withResources(connection::createStatement)
                    .of(statement -> statement.executeUpdate(sql))
                    .getOrElseThrow(e -> new RuntimeException("Query execution failed", e));
        }).doFinally(ignore -> this.connectionPool.add(connectionMono));
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
            Utils.valueRequirePositive(maxConnections, Option.none());
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder minConnections(int minConnections) {
            Utils.valueRequirePositive(minConnections, Option.none());
            this.minConnections = minConnections;
            return this;
        }

        public Builder periodForHealthCheckInMillis(int millis) {
            Utils.valueRequirePositive(millis, Option.none());
            this.periodForHealthCheckInMillis = millis;
            return this;
        }

        public Builder jdbcUrl(String jdbcUrl) {
            Utils.objectRequireNonNull(jdbcUrl, Option.some("Jdbc url cannot be null!"));
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder healthCheck(HealthCheck healthCheck) {
            Utils.objectRequireNonNull(healthCheck, Option.some("HealthCheck cannot be null!"));
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
