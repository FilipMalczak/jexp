package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.solver.Solver;
import com.github.filipmalczak.jexp.api.solver.SolverRun;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.AllArgsConstructor;
import org.junit.Test;

import javax.xml.crypto.Data;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

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
        List<Float> vals;
    }
    static class Rounder implements Solver<RoundingTask, SomeMoreVals, Params>{
        public Params parameters;

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

        @Override
        public void setParameters(Params parameters) {
            this.parameters = parameters;
        }

        @Override
        public Params getParameters() {
            return parameters;
        }
    }

    @Test
    public void testShouldCompile(){
        EvaluationBuilder.
            over(new Rounder()).
            concurrency().
                pool(0).
                    parallelism(4);
    }

}