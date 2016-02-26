package com.containersolutions.mesos.utils;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StreamHelper {

    /**
     * Create a new predicate that will evaluate a test predicate and eventually action consumer if
     * test predicate returns negative
     *
     * @param test predicate for test
     * @param action action to be performed on negative test
     * @param <T> the type of the input to the predicate
     * @return Predicate that will evaluate test predicate and eventually action consumer
     */
    public static <T> Predicate<T> onNegative(Predicate<T> test, Consumer<T> action) {
        return t -> {
            if (test.test(t)) {
                return true;
            }
            action.accept(t);
            return false;
        };
    }
}
