package com.github.filipmalczak.jexp.common.experiments.evaluation.aspects;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import com.github.filipmalczak.jexp.common.experiments.evaluation.Envelope;

import java.util.Collection;
import java.util.function.Supplier;

@FunctionalInterface
public interface EvaluationAspect<T extends Task, P extends Copyable<P>, S extends Solution<T>> {
    Collection<S> apply(Envelope.Key<P> key, Supplier<Collection<S>> underlyingAspect);

    default Envelope<P, S> getEnvelope(Envelope.Key<P> key, Supplier<Collection<S>> underlyingAspect){
        return new Envelope<>(key, apply(key, underlyingAspect));
    }
}
