package io.github.ufukhalis;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.sql.ResultSet;
import java.util.concurrent.Callable;

public final class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils() {

    }

    public static void objectRequireNonNull(Object o, Option<String> message) {
        if (o == null) {
            throw new IllegalArgumentException(message.getOrElse("Object cannot be null!"));
        }
    }

    public static void checkCondition(boolean c, Option<String> message) {
        if (!c) {
            throw new IllegalArgumentException(message.getOrElse("Condition is not valid!"));
        }
    }

    static void valueRequirePositive(int value, Option<String> message) {
        if (value < 0) {
            throw new IllegalArgumentException(message.getOrElse("Value cannot be negative!"));
        }
    }

    public static String escapeSql(String str) {
        return str.replace("'", "''");
    }

    static void closeSilently(AutoCloseable c) {
        Try.run(c::close).onFailure(throwable -> log.error("Connection couldn't close {}", c));
    }

    static Flux<ResultSet> convertMonoResultSetToFlux(ResultSet resultSet) {
        Callable<ResultSet> callable = () -> resultSet;
        return Flux.generate(
                callable,
                (rs, sink) ->
                        Try.of(() -> {
                            if (rs.next()) {
                                log.debug("Fetching next result set {}", rs);
                                sink.next(rs);
                            } else {
                                log.debug("Result set fetching has completed {}", rs);
                                sink.complete();
                            }
                            return rs;
                        }).getOrElseThrow(e ->
                                new RuntimeException("Unexpected error when iterating result set {}", e)),
                Utils::closeSilently);
    }
}
