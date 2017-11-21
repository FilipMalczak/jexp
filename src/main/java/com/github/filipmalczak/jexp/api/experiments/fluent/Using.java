package com.github.filipmalczak.jexp.api.experiments.fluent;

import java.lang.annotation.*;

//todo: definite TODO
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Repeatable(Usage.class)
public @interface Using {
    String executor() default "";
    String translator() default "";
    String traversal() default "";

}
