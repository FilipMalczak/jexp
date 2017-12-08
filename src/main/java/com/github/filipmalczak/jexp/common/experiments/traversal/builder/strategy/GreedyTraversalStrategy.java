package com.github.filipmalczak.jexp.common.experiments.traversal.builder.strategy;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.common.PropertySupport;
import com.github.filipmalczak.jexp.common.experiments.traversal.builder.Domain;
import com.github.filipmalczak.jexp.common.experiments.traversal.builder.DomainTraversalStrategy;
import com.github.filipmalczak.jexp.common.experiments.traversal.builder.TraversalStage;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Slf4j
public class GreedyTraversalStrategy<Params extends Copyable<Params>> implements DomainTraversalStrategy<Params> {
    @Override
    public <ParamsGrade extends Comparable<ParamsGrade>> TraversalStage<Params, ParamsGrade> apply(List<Domain> domains) {
        return new StrategyRun<ParamsGrade>(domains);
    }

    @AllArgsConstructor
    private class StrategyRun<ParamsGrade extends Comparable<ParamsGrade>> implements TraversalStage<Params, ParamsGrade> {
        private @NonNull List<Domain> domains;

        @Override
        public Params traverseFrom(Params startParams, GradingContext<Params, ParamsGrade> gradingContext) {
            Params current = startParams.copy();
            for (Domain domain: domains) {
                //fixme: I don't understand where this cast comes from, but IDEA proposes that, so be it
                current = (Params) traverseDomain(domain, current, gradingContext);
            }
            return current;
        }

        @SneakyThrows
        private <V, ParamsGrade extends Comparable<ParamsGrade>> Params traverseDomain(Domain<V> domain, Params initialParams, GradingContext<Params, ParamsGrade> context){
            log.info("Traversing "+domain+" from "+initialParams);
            Params current = initialParams;
            V initialValue = (V) PropertySupport.getProperty(current, domain.getName());
            List<V> domainValues = domain.getProvider().apply(initialValue);
            Map<Params, Future<ParamsGrade>> domainResults = new HashMap<>();
            for (V val: domainValues){
                current=current.copy();
                PropertySupport.setProperty(current, domain.getName(), val);
                domainResults.put(current, context.grade(current));
            }
            Map<Params, ParamsGrade> results = new HashMap<>();
            for (val entry: domainResults.entrySet())
                results.put(entry.getKey(), entry.getValue().get());
            Params result = results.entrySet().stream().max(Comparator.comparing(e -> e.getValue())).get().getKey();
            log.info("Traversed "+domain+" with "+result);
            return result;
        }
    }


}
