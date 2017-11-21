package com.github.filipmalczak.jexp.api.experiments.fluent;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public enum  ProviderScope {
    /**
     * Shared globally, allowed only in @ExperimentSuiteCommon-marked classes.
     */
    GLOBAL,
    /**
     * Shared in scope of single @ExperimentSuite-marked class.
     */
    LOCAL,
    /**
     * Local, each reference to annotated element creates new instance.
     */
    FACTORY,
    /**
     * Shouldn't be used, exists as a default annotation method value, which
     * will be resolved in runtime (depending on annotation target).
     */
    @API(status = API.Status.INTERNAL)
    DEFAULT
}
