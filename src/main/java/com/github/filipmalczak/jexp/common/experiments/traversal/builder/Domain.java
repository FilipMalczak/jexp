package com.github.filipmalczak.jexp.common.experiments.traversal.builder;

import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.function.Function;

@Value
@Getter
public class Domain<V> {
    String name;
    Function<V, List<V>> provider;
}
