package com.github.filipmalczak.jexp.common.experiments.traversal.builder;

import com.github.filipmalczak.jexp.api.common.Copyable;

import java.util.List;
import java.util.concurrent.Future;

public interface DomainTraversalStrategy<Params extends Copyable<Params>> {

    <ParamsGrade extends Comparable<ParamsGrade>> TraversalStage<Params, ParamsGrade> apply(List<Domain> domains);

    @FunctionalInterface
    interface GradingContext<Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>> {
        Future<ParamsGrade> grade(Params params);
    }
}
