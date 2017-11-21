package com.github.filipmalczak.jexp.api.experiments.fluent;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ExperimentSuite {
    String value() default "";
    String id() default "";
    String name() default "";
    String description() default "";
}
