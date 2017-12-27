package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.evaluation.ParametersEvaluator;
import com.github.filipmalczak.jexp.api.experiments.evaluation.SolutionEvaluator;
import com.github.filipmalczak.jexp.api.solver.Solver;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import lombok.SneakyThrows;
import lombok.Value;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
public class SolverContext<T extends Task, S extends Solution<T>, D extends Dataset<T>, V extends Comparable<V>> {
    private SolutionEvaluator<S, V> solutionEvaluator;
    private D dataset;
    private Function<Collection<V>, V> reduceFunction;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public <Params extends Copyable<Params>> ParametersEvaluator<Params, V> buildEvaluator(Solver<T, D, Params, S> solver){
        //todo: move impl up
        return new ParametersEvaluator<Params, V>() {
            @Override
            @SneakyThrows
            public Future<V> evaluate(Params parameters) {
                Future<Collection<S>> solutions = executorService.submit(() -> (Collection<S>) solver.buildRun(dataset).perform());
                return executorService.submit(() -> reduceFunction.
                    apply(
                        solutions.get().
                            stream().
                            map((x) -> executorService.submit(() -> solutionEvaluator.evaluate(x))).
//                            map(Future::get).
                            map((x) -> { //fixme: bleeeeeh... how do I do that better?
                                try {
                                    return x.get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }).
                            collect(Collectors.toList()))
                    );
            }
        };
    }
}
