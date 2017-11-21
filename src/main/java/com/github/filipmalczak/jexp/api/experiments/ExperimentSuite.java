package com.github.filipmalczak.jexp.api.experiments;

import com.github.filipmalczak.jexp.api.experiments.ExperimentDefinition;

import java.util.List;

public interface ExperimentSuite {
    String getName();
    String getDescription();

    List<ExperimentDefinition<?>> getDefinitions();
}
