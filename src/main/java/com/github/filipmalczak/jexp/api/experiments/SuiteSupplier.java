package com.github.filipmalczak.jexp.api.experiments;

import java.util.function.Supplier;

@FunctionalInterface
public interface SuiteSupplier extends Supplier<Iterable<ExperimentSuite>> {}
