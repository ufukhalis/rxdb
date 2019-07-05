package io.github.ufukhalis.db;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static io.vavr.API.*;

public class ConnectionPool {

    private Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    private final String jdbcUrl;
    private final int maxConnections;
    private final int minConnections;
    private final Queue<Mono<Connection>> CONNECTION_POOL = new ConcurrentLinkedQueue<>();
    private final Queue<Mono<Connection>> DEAD_POOL = new ConcurrentLinkedQueue<>();
    private final Executor THREAD_POOL;

    private final static int MAX_DEAD_POOL_SIZE = 5;

    public ConnectionPool(int maxConnections, int minConnections, String jdbcUrl, Executor threadPool) {
        this.jdbcUrl = jdbcUrl;
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.THREAD_POOL = threadPool;
        this.CONNECTION_POOL.addAll(createConnectionWithMaxValue(maxConnections).asJava());
    }

    private List<Mono<Connection>> createConnectionWithMaxValue(int max) {
        log.debug("Adding new connections to pool...");
        return List.range(0, max)
                .map(ignore -> DbConnection.createFromMono(jdbcUrl));
    }

    public Mono<Connection> getConnection() {
        log.debug("Getting a connection from pool...");
        return Match(this.CONNECTION_POOL.size()).of(
                Case($(n -> n > minConnections), () -> {
                    log.debug("Getting available connection...");
                    return this.CONNECTION_POOL.poll();
                }),
                Case($(),() -> {
                    log.debug("Not enough connection in the pool...");

                    List<Mono<Connection>> connectionList =
                            createConnectionWithMaxValue(maxConnections - this.CONNECTION_POOL.size());

                    this.CONNECTION_POOL.addAll(connectionList.asJava());

                    return this.CONNECTION_POOL.poll();
                })).subscribeOn(Schedulers.fromExecutor(this.THREAD_POOL));
    }

    private void releaseConnection(Connection connection) {
        log.debug("Releasing connection {}", connection);
        Try.run(connection::close)
                .getOrElseThrow(e -> new RuntimeException("Connection couldn't released", e));
    }

    public void add(Mono<Connection> connectionMono) {
        this.DEAD_POOL.add(connectionMono);
        this.clearTheDeadPool();
    }

    private void clearTheDeadPool() {
        if (this.DEAD_POOL.size() > MAX_DEAD_POOL_SIZE) {
            final Queue<Mono<Connection>> temp = new ConcurrentLinkedQueue<>(this.DEAD_POOL);
            this.DEAD_POOL.clear();
            Flux.fromIterable(temp)
                    .flatMap(v -> v)
                    .doOnEach(signal -> Option.of(signal.get()).forEach(this::releaseConnection))
                    .subscribe();
        }
    }
}
