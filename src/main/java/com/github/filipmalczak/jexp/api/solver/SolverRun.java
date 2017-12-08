package com.github.filipmalczak.jexp.api.solver;

import com.github.filipmalczak.jexp.api.task.Solution;
import com.github.filipmalczak.jexp.api.task.Task;
import org.apiguardian.api.API;

import java.util.Collection;

@API(status = API.Status.EXPERIMENTAL)
public interface SolverRun<T extends Task, S extends Solution<T>> {
    Collection<S> perform();
}
