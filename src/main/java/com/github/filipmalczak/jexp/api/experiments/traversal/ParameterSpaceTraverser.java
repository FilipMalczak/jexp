package com.github.filipmalczak.jexp.api.experiments.traversal;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.CompositeListener;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.NoOpListener;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.TraversalListener;

@FunctionalInterface
public interface ParameterSpaceTraverser<Params extends Copyable<Params>> {
    void traverse(TraversalListener<Params> listener);

    default void traverse(){
        traverse(new NoOpListener<>());
    }

    default void traverse(TraversalListener<Params>... listeners){
        traverse(new CompositeListener<>());
    }
}
