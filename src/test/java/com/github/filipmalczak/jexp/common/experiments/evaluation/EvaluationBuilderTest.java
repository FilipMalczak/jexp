package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.solver.Solver;
import com.github.filipmalczak.jexp.api.solver.SolverRun;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import com.github.filipmalczak.jexp.common.experiments.evaluation.aspects.EvaluationAspect;
import com.github.filipmalczak.jexp.spring.experiments.evaluation.SpringJpaAdapter;
import lombok.*;
import lombok.experimental.Accessors;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

//@RunWith(SpringRunner.class)
public class EvaluationBuilderTest {
    static class RoundingTask implements Task{}
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class Params implements Copyable<Params> {
        float x;
        float y;

        @Override
        public Params copy() {
            return new Params(x, y);
        }
    }
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class SumOfRoundedVals implements Solution<RoundingTask> {
        int sum;

    }
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class SomeMoreVals implements Dataset<RoundingTask> {
        @Accessors List<Float> vals;

        public SomeMoreVals(Float... vals) {
            this(asList(vals));
        }

        public SomeMoreVals(Double... vals) {
            this(asList(vals).stream().map(Double::floatValue).collect(toList()));
        }
    }
    static class Rounder implements Solver<RoundingTask, SomeMoreVals, Params, SumOfRoundedVals>{
        @Accessors public Params parameters;

        @Override
        public SolverRun buildRun(SomeMoreVals dataset) {
            return new SolverRun() {
                @Override
                public Collection<SumOfRoundedVals> perform() {
                    return asList(
                        new SumOfRoundedVals(
                            Math.round(parameters.x)+
                                Math.round(parameters.y)+
                                dataset.vals.stream().
                                    mapToInt(Math::round).
                                    sum()
                        )
                    );
                }
            };
        }

        //fixme: wtf happened here? let's trust IDEA for now, but need to investigate
        @Override
        public void setParameters(Params parameters) {
            this.parameters = parameters;
        }

//        @Override
//        public void setParameters(Copyable parameters) {
//            setParameters(parameters);
//        }

        @Override
        public Params getParameters() {
            return parameters;
        }
    }

    private SomeMoreVals dataset = new SomeMoreVals(1.1, 2.2, 3.3, 6.5);

    static class LoggingAspect<T extends Task, P extends Copyable<P>, S extends Solution<T>> implements EvaluationAspect<T, P, S>{
        public final List<Envelope<P, S>> log = new LinkedList<>();

        @Override
        public Collection<S> apply(Envelope.Key<P> key, Supplier<Collection<S>> underlyingAspect) {
            Collection<S> result = underlyingAspect.get();
            log.add(new Envelope<>(key, result));
            return result;
        }
    }


    @Test
    @SneakyThrows
    public void testShouldCompile(){
        LoggingAspect<RoundingTask, Params, SumOfRoundedVals> logging = new LoggingAspect<>();

        val builder = EvaluationBuilder.
            over(new Rounder(), dataset).
            gradedWith((SumOfRoundedVals solution) -> solution.sum).
            concurrency().
                pool(0).
            workStealingPool(4).
            then().
            store().
                //todo: fill repository
                with(new SpringJpaAdapter<>(Params.class, SumOfRoundedVals.class, null)).
            repeat().
                times(10).
                reduceWith((Collection<Integer> c)->
                    c.stream().mapToInt(i -> i).
                        max().getAsInt()
                ).
            then().
            weaveAspect(logging);
        ;
//        builder.buildSolutionEvaluator();
        System.out.println();
        Params p = new Params(2.2f, 5.3f);
        assertEquals(20.0f, (float) builder.buildParametersEvaluator().evaluate(p).get(), 0.01f);
        assertEquals(
            asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).stream().
                map(i ->  new Envelope<>(new Envelope.Key<Params>(p, i), asList(new SumOfRoundedVals(20)))).
                collect(toList()),
            logging.log
        );
    }

}