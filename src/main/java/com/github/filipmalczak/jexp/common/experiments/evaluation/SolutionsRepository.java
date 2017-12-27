package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface SolutionsRepository<Params extends Copyable<Params>, S extends Solution<?>, E extends Envelope<Params, S>> {
    Optional<Collection<S>> get(Params params);

    void put(Params params, Collection<S> results);

    Class<Params> getParameterClass();
    Class<S> getSolutionClass();
    void setBackend(JpaRepository<E, Params> repository);
    //todo: some locking support?
}
