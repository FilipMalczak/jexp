package com.github.filipmalczak.jexp.api.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import lombok.SneakyThrows;
import org.apiguardian.api.API;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

//todo: extract Evaluator?
@API(status = API.Status.EXPERIMENTAL)
@FunctionalInterface
public interface ParametersEvaluator<Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>> {
    Future<ParamsGrade> evaluate(Params parameters);

    interface RepeatingEvaluatorBuilder<Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>> {
        RepeatingEvaluatorBuilder<Params, ParamsGrade> times(int times);
        RepeatingEvaluatorBuilder<Params, ParamsGrade> reduceWith(Function<Collection<ParamsGrade>, ParamsGrade> reduceFoo);
        RepeatingEvaluatorBuilder<Params, ParamsGrade> executeWith(ExecutorService executorService);
        ParametersEvaluator<Params, ParamsGrade> build();
    }

    default RepeatingEvaluatorBuilder<Params, ParamsGrade> repeat(){
        ParametersEvaluator<Params, ParamsGrade> thisToBeRepeated = this;
        return new RepeatingEvaluatorBuilder<Params, ParamsGrade>() {
            private Integer times = null;
            private Function<Collection<ParamsGrade>, ParamsGrade> reduceFoo;
            private ExecutorService executorService = null;

            //todo: check for null parameters, check for positive times

            @Override
            public RepeatingEvaluatorBuilder times(int times) {
                if (this.times == null)
                    throw new IllegalStateException(); //todo: better exception
                this.times = times;
                return this;
            }

            @Override
            public RepeatingEvaluatorBuilder reduceWith(Function<Collection<ParamsGrade>, ParamsGrade> reduceFoo) {
                if (this.reduceFoo == null)
                    throw new IllegalStateException(); //todo: better exception
                this.reduceFoo = reduceFoo;
                return this;
            }

            @Override
            public RepeatingEvaluatorBuilder<Params, ParamsGrade> executeWith(ExecutorService executorService) {
                if (this.executorService == null)
                    throw new IllegalStateException(); //todo: better exception
                this.executorService = executorService;
                return this;
            }

            @SneakyThrows
            private Future<ParamsGrade> repeatAndReduce(Params params){
                Set<Future<ParamsGrade>> gradeFutures = new HashSet<>();
                for (int i=0; i < times; ++i)
                    gradeFutures.add(thisToBeRepeated.evaluate(params));
                Set<ParamsGrade> grades = new HashSet<>();
                for (Future<ParamsGrade> future: gradeFutures)
                    grades.add(future.get());
                return executorService.submit( () -> reduceFoo.apply(grades));
            }

            @Override
            @SneakyThrows
            public ParametersEvaluator<Params, ParamsGrade> build() {
                if (times == null)
                    throw new IllegalStateException(); //todo: better exception
                if (reduceFoo == null)
                    throw new IllegalStateException(); //todo: better exception
                if (executorService == null)
                    executorService = Executors.newSingleThreadExecutor();
                return this::repeatAndReduce;
            }
        };
    }

    static <Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>>
        ParametersEvaluator<Params, ParamsGrade> of(ExecutorService executorService, Function<Params, ParamsGrade> evaluationFoo){
        return (Params p) -> executorService.submit(()-> evaluationFoo.apply(p));
    }

    static <Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>>
        ParametersEvaluator<Params, ParamsGrade> of(Function<Params, ParamsGrade> evaluationFoo){
        return of(Executors.newSingleThreadExecutor(), evaluationFoo);
    }
}
