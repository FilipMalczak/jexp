package com.github.filipmalczak.jexp.api.experiments.fluent;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Experiment {
    String value() default "";
    String id() default "";
    String name() default "";
}
