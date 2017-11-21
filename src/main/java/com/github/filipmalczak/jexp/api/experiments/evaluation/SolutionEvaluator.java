package com.github.filipmalczak.jexp.api.experiments.evaluation;

import com.github.filipmalczak.jexp.api.task.Solution;
import org.apiguardian.api.API;

/**
 * contract: evaluate(S1)>evaluate(S2) means that S1 is better than S2
 */
//todo: extract Evaluator?
@FunctionalInterface
@API(status = API.Status.EXPERIMENTAL)
public interface SolutionEvaluator<S extends Solution<?>, V extends Comparable<V>> {
    V evaluate(S solution);
}
