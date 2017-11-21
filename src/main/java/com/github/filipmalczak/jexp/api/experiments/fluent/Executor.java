package com.github.filipmalczak.jexp.api.experiments.fluent;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Executor {
    ProviderScope value() default ProviderScope.DEFAULT;
    String id() default "";
    ProviderScope scope() default ProviderScope.DEFAULT;
    String[] experiments() default {};
}
