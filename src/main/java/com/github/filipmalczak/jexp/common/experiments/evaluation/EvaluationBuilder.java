package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.evaluation.ParametersEvaluator;
import com.github.filipmalczak.jexp.api.experiments.evaluation.SolutionEvaluator;
import com.github.filipmalczak.jexp.api.solver.Solver;
import com.github.filipmalczak.jexp.api.solver.SolverRun;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import com.github.filipmalczak.jexp.common.experiments.evaluation.filters.EvaluationFilter;
import lombok.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EvaluationBuilder<
            T extends Task,
            D extends Dataset<T>,
            P extends Copyable<P>,
            S extends Solution<T>,
            SolutionV extends Comparable<SolutionV>,
            RunV extends Comparable<RunV>,
            ParamsV extends Comparable<ParamsV>
        > implements Copyable<EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV>> {
    private Solver<T, D, P, S> solver;
    private D dataset;
    @Getter private SolutionEvaluator<S, SolutionV> solutionEvaluator;
    private Function<Collection<SolutionV>, RunV> runEvaluatorReduce;
    private ExecutorService quickExecutor = Executors.newCachedThreadPool();
    private ExecutorService heavyExecutor = Executors.newWorkStealingPool();
    private Map<String, ExecutorService> executors = new HashMap<>();
    private SolutionsRepository<P, S, ? extends Envelope<P, S>> repository;
    private Collection<Integer> iterNumbers = new HashSet<>();
    private Function<Collection<RunV>, ParamsV> reduceFoo;
    //todo: re-enable filters
//    private List<EvaluationFilter> filters = new LinkedList<>();

    public static <T extends Task, D extends Dataset<T>, P extends Copyable<P>, S extends Solution<T>>  OverClosure<T, D, P, S> over(Solver<T, D, P, S> solver, D dataset){
        return new OverClosure<>(solver, dataset);
    }

    @AllArgsConstructor
    public static class OverClosure<T extends Task, D extends Dataset<T>, P extends Copyable<P>, S extends Solution<T>> {
        private Solver<T, D, P, S> solver;
        private D dataset;

        public <SolutionV extends Comparable<SolutionV>> EvaluationBuilder<T, D, P, S, SolutionV, SolutionV, SolutionV> gradedWith(SolutionEvaluator<S, SolutionV> solutionEvaluator){
            EvaluationBuilder<T, D, P, S, SolutionV, SolutionV, SolutionV> out = new EvaluationBuilder<>();
            out.solver = solver;
            out.dataset = dataset;
            out.solutionEvaluator = solutionEvaluator;
            out.runEvaluatorReduce = (Collection<SolutionV> c) -> c.stream().max(Comparator.naturalOrder()).get();
            out.reduceFoo = (Collection<SolutionV> c) -> c.stream().max(Comparator.naturalOrder()).get();
            return out;
        }

    }


    public ConcurrencySubBuilder concurrency(){
        return new ConcurrencySubBuilder();
    }

    public class ConcurrencySubBuilder {
        @AllArgsConstructor
        @NoArgsConstructor
        public class ExecutorClosure {
            private Function<ExecutorService, EvaluationBuilder> setter;

            ConcurrencySubBuilder threadFactory(ThreadFactory threadFactory){
                EvaluationBuilder toReturn = setter.apply(
                    Executors.newCachedThreadPool(threadFactory)
                );
                return toReturn.concurrency();
            }

            ConcurrencySubBuilder parallelism(int parallelism){
                EvaluationBuilder toReturn = setter.apply(
                    Executors.newWorkStealingPool(parallelism)
                );
                return toReturn.concurrency();
            }

            ConcurrencySubBuilder useExecutorService(ExecutorService executorService){
                EvaluationBuilder toReturn = setter.apply(
                    executorService
                );
                return toReturn.concurrency();
            }
        }

        public ExecutorClosure quick(){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copied = (EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV>) topBuilder().copy();
                copied.quickExecutor = v;
                return copied;
            });
        }

        public ExecutorClosure heavy(){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copied = (EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV>) topBuilder().copy();
                copied.heavyExecutor = v;
                return copied;
            });
        }

        public ExecutorClosure pool(int no){
            return pool("Unnamed-Pool-#"+no);
        }

        public ExecutorClosure pool(String name){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copied = (EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV>) topBuilder().copy();
                copied.executors.put(name, v);
                return copied;
            });
        }

        EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> then(){
            return topBuilder();
        }
    }

    public StorageClosure store(){
        return new StorageClosure();
    }

    public class StorageClosure {
        public EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> with(SolutionsRepository<P, S, ? extends Envelope<P, S>> repository){
            EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copied = topBuilder().copy();
            copied.repository = repository;
            return copied;
        }
    }

    public ReiterationClosure repeat(){
        return new ReiterationClosure();
    }

    public class ReiterationClosure {
        public ReiterationClosure times(int times){
            return iterationsNo(IntStream.range(0, times).boxed().collect(toList()));
        }

        public ReiterationClosure iterationsNo(Collection<Integer> iterNumbers){
            EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> out = topBuilder().copy();
            out.iterNumbers.addAll(iterNumbers);
            return out.repeat();
        }

        public <NewRunV extends Comparable<NewRunV>, NewParamsV extends Comparable<NewParamsV>> EvaluationBuilder<T, D, P, S, SolutionV, NewRunV, NewParamsV>.ReiterationClosure reduceWith(Function<Collection<NewRunV>, NewParamsV> foo){
            EvaluationBuilder<T, D, P, S, SolutionV, NewRunV, NewParamsV> out = (EvaluationBuilder<T, D, P, S, SolutionV, NewRunV, NewParamsV>) topBuilder().copy();
            out.reduceFoo = foo;
            return out.repeat();
        }

        public EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> then(){
            return topBuilder();
        }
    }

    private EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> topBuilder(){
        return this;
    }

    @Override
    public EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copy() {
        return new EvaluationBuilder<>(
            solver,
            dataset,
            solutionEvaluator,
            runEvaluatorReduce,
            quickExecutor,
            heavyExecutor,
            new HashMap<>(executors),
            repository,
            new ArrayList<>(iterNumbers),
            reduceFoo//,
//            new ArrayList<>(filters)
        );
    }

    //todo: only repetitions working; no repository support, nor proper custom pools usage
    public ParametersEvaluator<P, ParamsV> buildParametersEvaluator(){
        return new ParametersEvaluator<P, ParamsV>() {
            public RunV grade(SolverRun<T, S> run){
                Collection<S> solutions = run.perform();
                if (solutions.size() < 1)
                    throw new IllegalArgumentException("Run didn't return any solutions!");
                return runEvaluatorReduce.apply(run.perform().stream().map(solutionEvaluator::evaluate).collect(toList()));
            }

            public ParamsV grade(List<RunV> parameterRunResults){
                return reduceFoo.apply(parameterRunResults);
            }

            public ParamsV grade(P parameters){
                if (iterNumbers.isEmpty())
                    iterNumbers.add(0);
                solver.setParameters(parameters);
                return grade(
                    iterNumbers.stream().
                        map(
                            idx ->
                                heavyExecutor.submit(() ->
                                    this.grade(solver.buildRun(dataset))
                                )
                        ).
                        map((f) -> {
                            try {
                                return f.get();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        }).
                        collect(toList())
                );
            }

            @Override
            public Future<ParamsV> evaluate(P parameters) {
                return quickExecutor.submit(() -> grade(parameters));
            }
        };
    }
}
