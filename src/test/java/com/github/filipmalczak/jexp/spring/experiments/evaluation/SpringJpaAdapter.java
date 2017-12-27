package com.github.filipmalczak.jexp.spring.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.common.experiments.evaluation.Envelope;
import com.github.filipmalczak.jexp.common.experiments.evaluation.EvaluationBuilderTest;
import com.github.filipmalczak.jexp.common.experiments.evaluation.SolutionsRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

@AllArgsConstructor
@Getter
public class SpringJpaAdapter<P extends Copyable<P>, S extends Solution<?>, E extends Envelope<P, S>> implements SolutionsRepository<P, S, E> {
    private Class<P> parameterClass;
    private Class<S> solutionClass;
    private JpaRepository<E, P> repository;
    //todo: metadata like time of calculations, etc


    @Override
    public Optional<Collection<S>> get(P p) {
        return Optional.empty();
    }

    @Override
    public void put(P p, Collection<S> results) {

    }

    @Override
    public void setBackend(JpaRepository<E, P> repository) {
        this.repository = repository;
    }
}
