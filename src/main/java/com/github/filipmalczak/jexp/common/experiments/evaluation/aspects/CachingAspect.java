package com.github.filipmalczak.jexp.common.experiments.evaluation.aspects;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import com.github.filipmalczak.jexp.common.experiments.evaluation.Envelope;
import com.github.filipmalczak.jexp.common.experiments.evaluation.SolutionsRepository;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@ToString
@EqualsAndHashCode
public class CachingAspect<T extends Task, P extends Copyable<P>, S extends Solution<T>, E extends Envelope<P, S>> implements EvaluationAspect<T, P, S>{
    private final SolutionsRepository<P, S, E> repository;

    public CachingAspect(SolutionsRepository<P, S, E> repository) {
        this.repository = repository;
    }

    @Override
    public Collection apply(Envelope.Key key, Supplier underlyingAspect) {
        Optional<Collection<S>> cached = repository.get(key);
        return cached.orElseGet(underlyingAspect);
    }
}
