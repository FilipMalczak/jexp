package com.github.filipmalczak.jexp.api.common;

import lombok.*;

import java.util.*;
import java.util.function.Function;

@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
public class UnresolvedParameters<Params> implements Copyable<UnresolvedParameters<Params>>{
    private @NonNull Class<Params> parametersClass;
    private @Getter Map<String, Object> values = new HashMap<>();
    private @Getter Map<String, Function> translators = new HashMap<>();

    @SneakyThrows
    public Params resolve(){
        Params out = parametersClass.newInstance();
        values.forEach((k, v) -> {
            PropertySupport.setProperty(
                out,
                k,
                translators.getOrDefault(k, x -> x).apply(v)
            );
        });
        return out;
    }

    @Override
    public String toString() {
        return "UnresolvedParameters{" +
            "parametersClass=" + parametersClass +
            ", values=" + values +
            '}';
    }

    @Override
    public UnresolvedParameters<Params> copy() {
        return new UnresolvedParameters<Params>(parametersClass, new HashMap<>(values), new HashMap<>(translators));
    }
}
