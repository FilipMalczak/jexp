package com.github.filipmalczak.jexp.common.experiments.traversal.builder;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.evaluation.ParametersEvaluator;
import com.github.filipmalczak.jexp.api.experiments.traversal.ParameterSpaceTraverser;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.TraversalListener;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class SimpleTraverser<Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>> implements ParameterSpaceTraverser<Params> {
    private Params initialParams;
    private List<TraversalStage<Params, ParamsGrade>> stages;
    private ParametersEvaluator<Params, ParamsGrade> evaluator;

    @Override
    public void traverse(TraversalListener<Params> listener) {
        Params current = initialParams;
        listener.beforeTraversal(current);
        DomainTraversalStrategy.GradingContext<Params, ParamsGrade> gradingContext = (p) -> {
            listener.onGradingParameters(p);
            return evaluator.evaluate(p);
        };
        for (TraversalStage<Params, ParamsGrade> stage: stages) {
            current = stage.traverseFrom(current, gradingContext);
        }
        listener.onTraversalEnd(current);
    }
}
