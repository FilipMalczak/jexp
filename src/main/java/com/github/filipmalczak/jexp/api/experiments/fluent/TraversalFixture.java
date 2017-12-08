package com.github.filipmalczak.jexp.api.experiments.fluent;

import java.lang.annotation.*;

/**
 * Used on {@code @Experiment}-annotated method parameter to indicate the id of method annotated with {@code @Traversal}
 * to be injected as that parameter.
 * If the {@code @Traversal}-annotated method is in {@code @ExperimentSuite}- or {@code @ExperimentSuiteCommon}-annotated
 * class that isn't using {@code id} attribute of these annotations, the {@code id} of this annotation references the
 * {@code id} on annotation of that method.
 * In other case {@code id} here is in form {@code <suite (strategy) id>.<method-level id>}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface TraversalFixture {
    String value() default "";
    String id() default "";
}
