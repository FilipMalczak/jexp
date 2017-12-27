package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.task.Solution;
import lombok.Data;

import java.util.Collection;

@Data
public class Envelope<Params extends Copyable<Params>, S extends Solution<?>> {
    private Params parameters;
    private Collection<S> solutions;
}
