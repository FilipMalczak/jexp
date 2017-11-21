package com.github.filipmalczak.jexp.api.experiments.fluent;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface TraversalFixture {
    String value() default "";
    String id() default "";
}
