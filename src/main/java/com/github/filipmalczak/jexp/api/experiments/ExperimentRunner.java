package com.github.filipmalczak.jexp.api.experiments;

import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface ExperimentRunner {
    void run(Iterable<ExperimentSuite> suites);

    default void run(ExperimentSuite... suites){
        run(Arrays.asList(suites));
    }

    default void run(Supplier<Iterable<ExperimentSuite>> suiteSupplier){
        run(suiteSupplier.get());
    }

    default void run(){
        ServiceLoader<SuiteSupplier> loader = ServiceLoader.load(SuiteSupplier.class);
        for (SuiteSupplier supplier: loader)
            run(supplier);
    }

}
