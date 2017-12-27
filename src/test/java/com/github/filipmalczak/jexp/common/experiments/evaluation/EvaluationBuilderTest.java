package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.evaluation.SolutionEvaluator;
import com.github.filipmalczak.jexp.api.solver.Solver;
import com.github.filipmalczak.jexp.api.solver.SolverRun;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import com.github.filipmalczak.jexp.spring.experiments.evaluation.SpringJpaAdapter;
import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.crypto.Data;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

//@RunWith(SpringRunner.class)
public class EvaluationBuilderTest {
    static class RoundingTask implements Task{}
    @AllArgsConstructor
    static class Params implements Copyable<Params> {
        float x;
        float y;

        @Override
        public Params copy() {
            return new Params(x, y);
        }
    }
    @AllArgsConstructor
    static class SumOfRoundedVals implements Solution<RoundingTask> {
        int sum;

    }
    @AllArgsConstructor
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


    @Test
    @SneakyThrows
    public void testShouldCompile(){
        val builder = EvaluationBuilder.
            over(new Rounder(), dataset).
            gradedWith((SumOfRoundedVals solution) -> solution.sum).
            concurrency().
                pool(0).
                    parallelism(4).
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
            then()
            //todo: some plugin/filter system?
        ;
//        builder.buildSolutionEvaluator();
        System.out.println(builder.buildParametersEvaluator().evaluate(new Params(2.2f, 5.3f)).get());
    }

}