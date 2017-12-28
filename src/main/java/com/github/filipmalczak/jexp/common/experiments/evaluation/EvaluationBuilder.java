package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.evaluation.ParametersEvaluator;
import com.github.filipmalczak.jexp.api.experiments.evaluation.SolutionEvaluator;
import com.github.filipmalczak.jexp.api.solver.Solver;
import com.github.filipmalczak.jexp.api.solver.SolverRun;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import com.github.filipmalczak.jexp.common.experiments.evaluation.aspects.CachingAspect;
import com.github.filipmalczak.jexp.common.experiments.evaluation.aspects.EvaluationAspect;
import com.github.filipmalczak.jexp.common.utils.FutureUtils;
import lombok.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

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
    private List<EvaluationAspect<T, P, S>> aspects = new LinkedList<>();

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
            private Function<ExecutorService, EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV>> topBuilderModifier;

            public ConcurrencySubBuilder cachedPool(ThreadFactory threadFactory){
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> toReturn = topBuilderModifier.apply(
                    Executors.newCachedThreadPool(threadFactory)
                );
                return toReturn.concurrency();
            }

            public ConcurrencySubBuilder workStealingPool(int parallelism){
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> toReturn = topBuilderModifier.apply(
                    Executors.newWorkStealingPool(parallelism)
                );
                return toReturn.concurrency();
            }

            public ConcurrencySubBuilder useExecutorService(ExecutorService executorService){
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> toReturn = topBuilderModifier.apply(
                    executorService
                );
                return toReturn.concurrency();
            }
        }

        public ExecutorClosure quick(){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copied = topBuilder().copy();
                copied.quickExecutor = v;
                return copied;
            });
        }

        public ExecutorClosure heavy(){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copied = topBuilder().copy();
                copied.heavyExecutor = v;
                return copied;
            });
        }

        public ExecutorClosure pool(int no){
            return pool("Unnamed-Pool-#"+no);
        }

        public ExecutorClosure pool(String name){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> copied = topBuilder().copy();
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

    public EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> weaveAspect(EvaluationAspect<T, P, S> aspect) {
        EvaluationBuilder<T, D, P, S, SolutionV, RunV, ParamsV> out = topBuilder().copy();
        out.aspects.add(aspect);
        return out;
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
            reduceFoo,
            new ArrayList<>(aspects)
        );
    }

    private boolean isCachingEnabled(){
        return this.repository != null;
    }

    private CachingAspect buildCachingAspect(){
        return new CachingAspect(this.repository);
    }

    //todo: only repetitions working; no repository support, nor proper custom pools usage
    public ParametersEvaluator<P, ParamsV> buildParametersEvaluator(){
        return new ParametersEvaluator<P, ParamsV>() {
            /**
             * Prepares list of aspect in useful order. Inject fallback aspect and optional caching aspect, ans return
             * all aspects starting from latest weaved in and ending in fallback one.
             * @param run experiment run to be used as fallback aspect
             * @return list of aspects, from the one that should be woven over all others, to the fallback one
             */
            private List<EvaluationAspect<T, P, S>> prepareAspects(SolverRun<T, S> run){
                int expectedAspects = aspects.size()+1; //custom aspects + fallback to run.perform
                if (isCachingEnabled())
                    expectedAspects += 1;
                List<EvaluationAspect<T, P, S>> list = new ArrayList<>(expectedAspects);
                list.add((k, thisIsGonnaBeNull) -> run.perform());
                if (isCachingEnabled())
                    list.add(buildCachingAspect());
                list.addAll(aspects);
                Collections.reverse(list);
                return list;
            }

            private Supplier<Collection<S>> getAspectChain(Envelope.Key<P> key, Iterator<EvaluationAspect<T, P, S>> aspectIterator){
                if (aspectIterator.hasNext()) {
                    EvaluationAspect<T, P, S> aspect = aspectIterator.next();
                    System.out.println("applying aspect "+aspect);
                    return () -> aspect.apply(key, () -> getAspectChain(key, aspectIterator).get());
                } else {
//                    throw new RuntimeException("This shouldn't happen! There should be fallback aspect that performs the run!");
                    return null;
                }
            }

            public Collection<S> performWithAspects(Envelope.Key<P> key, SolverRun<T, S> run){
                List<EvaluationAspect<T, P, S>> aspects = prepareAspects(run);
                if (aspects.size() == 1)
                    return run.perform();
                Iterator<EvaluationAspect<T, P, S>> aspectIterator = aspects.iterator();
                EvaluationAspect<T, P, S> rootAspects = aspectIterator.next();
                Supplier<Collection<S>> aspectChain = getAspectChain(key, aspectIterator);
                return rootAspects.apply(key, aspectChain);
            }

            public RunV grade(P parameters, int iterNo, SolverRun<T, S> run){
                Envelope.Key<P> key = new Envelope.Key<>(parameters, iterNo);
                Collection<S> solutions = performWithAspects(key, run);
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
                                    this.grade(parameters, idx, solver.buildRun(dataset))
                                )
                        ).
                        map(FutureUtils::safeGet).
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
