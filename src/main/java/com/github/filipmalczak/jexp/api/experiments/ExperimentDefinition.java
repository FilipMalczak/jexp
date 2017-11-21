package com.github.filipmalczak.jexp.api.experiments;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.traversal.ParameterSpaceTraverser;

public interface ExperimentDefinition<Params extends Copyable<Params>> {
    String getName();
    ParameterSpaceTraverser<Params> getTraverser();
}
