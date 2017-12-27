package com.github.filipmalczak.jexp.api.solver;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.common.Parametrized;
import com.github.filipmalczak.jexp.api.task.Dataset;
import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface Solver<T extends Task, D extends Dataset<T>, Params extends Copyable<Params>, S extends Solution<T>> extends Parametrized<Params> {
    SolverRun<T, S> buildRun(D dataset);
    //todo: should also provide an optional lock - by default new instance per method call, so its dispensable; use it in EvaluationBuilder
}
