package com.github.filipmalczak.jexp.api.experiments.traversal;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.evaluation.ParametersEvaluator;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.CompositeListener;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.NoOpListener;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.TraversalListener;

import java.util.concurrent.ExecutorService;

public interface ParameterSpaceTraverser<Params extends Copyable<Params>> {
    void setEvaluator(ParametersEvaluator<Params, ?> evaluator);
    void setExecutorService(ExecutorService executorService);

    void traverse(TraversalListener<Params> listener);

    default void traverse(){
        traverse(new NoOpListener<>());
    }

    default void traverse(TraversalListener<Params>... listeners){
        traverse(new CompositeListener<>());
    }
}
