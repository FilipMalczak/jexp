package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.task.Solution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import static com.github.filipmalczak.jexp.common.utils.GenericUtils.getGenericParameter;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Envelope<P extends Copyable<P>, S extends Solution<?>> {
    @Data
    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    public final static class Key<P extends Copyable<P>> {
        private P parameters;
        private int iterationNumber;
        private String qualifier;

        public Key(P parameters, int iterationNumber) {
            this(parameters, iterationNumber, "");
        }
    }
    private Key<P> key;
    private Collection<S> solutions;

}
