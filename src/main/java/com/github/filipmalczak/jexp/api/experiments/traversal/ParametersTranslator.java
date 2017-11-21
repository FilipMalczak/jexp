package com.github.filipmalczak.jexp.api.experiments.traversal;

import com.github.filipmalczak.jexp.api.common.Copyable;

import java.util.Map;

public interface ParametersTranslator<Params extends Copyable<Params>> {
    Params parse(Map<String, String> stringified);
    Map<String, String> dump(Params params);
}
