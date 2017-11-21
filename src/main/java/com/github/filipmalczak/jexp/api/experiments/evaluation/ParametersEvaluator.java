package com.github.filipmalczak.jexp.api.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import org.apiguardian.api.API;

//todo: extract Evaluator?
@API(status = API.Status.EXPERIMENTAL)
public interface ParametersEvaluator<Params extends Copyable<Params>, V extends Comparable<V>> {
    V evaluate(Params parameters);
}
