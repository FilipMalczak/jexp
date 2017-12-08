package com.github.filipmalczak.jexp.common.experiments.traversal.builder;

import com.github.filipmalczak.jexp.api.common.Copyable;
public interface TraversalStage<Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>>  {

    Params traverseFrom(Params startParams, DomainTraversalStrategy.GradingContext<Params, ParamsGrade> gradingContext);
}
