package com.github.filipmalczak.jexp.common.utils;

import lombok.SneakyThrows;

import java.util.concurrent.Future;

public class FutureUtils {
    @SneakyThrows
    /**
     * Wraps Future::get with @SneakyThrows, making it useful with streams.
     */
    public static <T> T safeGet(Future<T> future){
        return future.get();
    }
}
