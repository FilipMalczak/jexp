package com.github.filipmalczak.jexp.api.experiments.fluent;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ExperimentSuiteCommon {
    String value() default "";
    String id() default "";
}
