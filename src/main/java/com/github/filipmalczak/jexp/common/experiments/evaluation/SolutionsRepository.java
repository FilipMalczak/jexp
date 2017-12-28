package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

import static com.github.filipmalczak.jexp.common.utils.GenericUtils.getGenericParameter;

public interface SolutionsRepository<Params extends Copyable<Params>, S extends Solution<?>, E extends Envelope<Params, S>> {
    Optional<Collection<S>> get(Envelope.Key<Params> key);

    void put(Envelope.Key<Params> key, Collection<S> results);

    Class<E> getEnvelopeClass();

    default Class<Params> getParameterClass(){
        return (Class<Params>) getGenericParameter(getEnvelopeClass(), Envelope.class, 0);
    }

    default Class<S> getSolutionClass(){
        return (Class<S>) getGenericParameter(getEnvelopeClass(), Envelope.class, 1);
    }

    void setBackend(JpaRepository<E, Params> repository);
    //todo: some locking support?
}
