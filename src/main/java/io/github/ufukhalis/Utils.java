package io.github.ufukhalis;

import io.vavr.control.Option;

public final class Utils {

    private Utils() {

    }

    public static void objectRequireNonNull(Object o, Option<String> message) {
        if (o == null) {
            throw new IllegalArgumentException(message.getOrElse("Object cannot be null!"));
        }
    }

    public static void valueRequirePositive(int value, Option<String> message) {
        if (value < 0) {
            throw new IllegalArgumentException(message.getOrElse("Value cannot be negative!"));
        }
    }
}
