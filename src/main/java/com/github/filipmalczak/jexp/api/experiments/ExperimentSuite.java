package com.github.filipmalczak.jexp.api.experiments;

import java.util.List;

public interface ExperimentSuite {
    String getName();
    String getDescription();

    List<ExperimentDefinition<?>> getDefinitions();
}
