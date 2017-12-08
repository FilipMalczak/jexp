package com.github.filipmalczak.jexp.common.experiments.evaluation;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.solver.Solver;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Task;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EvaluationBuilder<T extends Task, D extends Dataset<T>, P extends Copyable<P>> implements Copyable<EvaluationBuilder<T, D, P>> {
    private Solver<T, D, P> solver;
    private ExecutorService quickExecutor = Executors.newCachedThreadPool();
    private ExecutorService heavyExecutor = Executors.newWorkStealingPool();
    private Map<String, ExecutorService> executors = new HashMap<>();

    public static <T extends Task, D extends Dataset<T>, P extends Copyable<P>>  EvaluationBuilder<T, D, P> over(Solver<T, D, P> solver){
        EvaluationBuilder out = new EvaluationBuilder();
        out.solver = solver;
        return out;
    }

    public ConcurrencySubBuilder concurrency(){
        return new ConcurrencySubBuilder();
    }

    @Override
    public EvaluationBuilder<T, D, P> copy() {
        return new EvaluationBuilder<>(solver, quickExecutor, heavyExecutor, new HashMap<>(executors));
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
                EvaluationBuilder<T, D, P> copied = (EvaluationBuilder<T, D, P>) topBuilder().copy();
                copied.quickExecutor = v;
                return copied;
            });
        }

        public ExecutorClosure heavy(){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P> copied = (EvaluationBuilder<T, D, P>) topBuilder().copy();
                copied.heavyExecutor = v;
                return copied;
            });
        }

        public ExecutorClosure pool(int no){
            return pool("Unnamed-Pool-#"+no);
        }

        public ExecutorClosure pool(String name){
            return new ExecutorClosure((ExecutorService v)  -> {
                EvaluationBuilder<T, D, P> copied = (EvaluationBuilder<T, D, P>) topBuilder().copy();
                copied.executors.put(name, v);
                return copied;
            });
        }
    }

    private EvaluationBuilder topBuilder(){
        return this;
    }
}
