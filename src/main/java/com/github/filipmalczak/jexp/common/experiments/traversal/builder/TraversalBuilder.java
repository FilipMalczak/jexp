package com.github.filipmalczak.jexp.common.experiments.traversal.builder;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.common.PropertySupport;
import com.github.filipmalczak.jexp.api.experiments.evaluation.ParametersEvaluator;
import com.github.filipmalczak.jexp.api.experiments.traversal.ParameterSpaceTraverser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TraversalBuilder<Params extends Copyable<Params>, ParamsGrade extends Comparable<ParamsGrade>> {
    private Class<Params> paramsClass;
    private Params initialParams;
    private Map<String, Function<Object, Object>> translators = new HashMap<>();
    private ParametersEvaluator<Params, ParamsGrade> evaluator;
    private List<TraversalStage<Params, ParamsGrade>> stages = new ArrayList<>();



    private TraversalBuilder(Class<Params> clazz){
        paramsClass = clazz;
    }

    public static <Params extends Copyable<Params>> TraversalBuilder getNew(Class<Params> paramsClass){
        return new TraversalBuilder(paramsClass);
    }

    public class ParametersDefinitionAndEvaluatorContext {
        private ParametersDefinitionAndEvaluatorContext(){}

        public <V, V2> ParametersDefinitionAndEvaluatorContext parameter(String name, Function<V, V2> translator){
            translators.put(name, (Function<Object, Object>) translator);
            return this;
        }

        public ParametersDefinitionAndEvaluatorContext evaluator(ParametersEvaluator<Params, ParamsGrade> evaluator){
            model().evaluator = evaluator;
            return this;
        }

        private TraversalBuilder<Params, ParamsGrade> model(){
            return TraversalBuilder.this;
        }

        public TraversalBuilder<Params, ParamsGrade> then(){
            return TraversalBuilder.this;
        }
    }

    public ParametersDefinitionAndEvaluatorContext using(){
        return new ParametersDefinitionAndEvaluatorContext();
    }

    public class StartFromCollector {
        private Map<String, Object> initialValues = new HashMap<>();

        private StartFromCollector(){}

        //todo: maybe support for startingFrom().choosingFirst().withStrategy(...).domain(...)

        public StartFromCollector valueOf(String name, Object value){
            initialValues.put(name, value);
            return this;
        }

        @SneakyThrows
        public TraversalBuilder<Params, ParamsGrade> then(){
            Params params = paramsClass.newInstance();
            initialValues.forEach((String k, Object v) -> {
                PropertySupport.setProperty(params, k,
                    translators.containsKey(k) ? translators.get(k).apply(v) : v
                );
            });
            initialParams = params;
            return TraversalBuilder.this;
        }


    }

    public StartFromCollector startFrom(){
        return new StartFromCollector();
    }

    public StrategyDomainsCollector withStrategy(DomainTraversalStrategy<Params> strategy){
        return new StrategyDomainsCollector(strategy, new ArrayList<>());
    }

    /**
     * domainValues are real values injected into Params properties.
     * domain are values that are translated via registered translator before injecting them. If no translator is registered,
     * behaves just as domainValues.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class StrategyDomainsCollector {
        private DomainTraversalStrategy<Params> strategy;
        private List<Domain> domains;

        public StartFromDomainSampleCollector choosing(InitialDomainSampler sampler){
            return new StartFromDomainSampleCollector(sampler, strategy);
        }

        public StartFromDomainSampleCollector choosingFirst(){
            return choosing(InitialDomainSampler.first());
        }

        public StartFromDomainSampleCollector choosingLast(){
            return choosing(InitialDomainSampler.last());
        }

        public StartFromDomainSampleCollector choosingMiddle(){
            return choosing(InitialDomainSampler.middle());
        }

        public <V> StrategyDomainsCollector domainValues(String name, List<V> values){
            //fixme: I don't get the reason for this cast neither
            return domainValues(name, x -> (List) values);
        }

        public <V> StrategyDomainsCollector domainValues(String name, Function<V, List<V>> valuesCalculator){
            domains.add(new Domain(name, valuesCalculator));
            return this;
        }

        public <V> StrategyDomainsCollector domain(String name, List<V> values){
            //fixme: I don't get the reason for this cast neither
            return domain(name, x -> (List) values);
        }

        public <V> StrategyDomainsCollector domain(String name, Function<V, List<V>> valuesCalculator){
            val translator = translators.getOrDefault(name, x -> x);
            //fixme: I'm getting lost in those casts
            return domainValues(name, x -> (List) valuesForDomain(name, valuesCalculator.apply((V) x)));
        }

        public TraversalBuilder<Params, ParamsGrade> then(){
            stages.add(strategy.apply(domains));
            return TraversalBuilder.this;
        }
    }

    private <V> Object valueForDomain(String name, V val){
        return translators.getOrDefault(name, x -> x).apply(val);
    }

    private <V> List<?> valuesForDomain(String name, List<V> values){
        //fixme: another cast I don't get
        return values.stream().map(x -> valueForDomain(name, x)).collect(Collectors.toList());
    }

    public class StartFromDomainSampleCollector {
        private InitialDomainSampler sampler;
        private StartFromCollector startFromCollector;
        private StrategyDomainsCollector domainsCollector;

        public StartFromDomainSampleCollector(InitialDomainSampler sampler, DomainTraversalStrategy<Params> strategy) {
            this.sampler = sampler;
            startFromCollector = startFrom();
            domainsCollector = withStrategy(strategy);
        }

        public <V> StartFromDomainSampleCollector domainValues(String name, List<V> values){
            startFromCollector.valueOf(name, sampler.chooseInitialValue(name, values));
            domainsCollector.domainValues(name, values);
            return this;
        }

        public <V> StartFromDomainSampleCollector domain(String name, List<V> values){
            startFromCollector.valueOf(name, sampler.chooseInitialValue(name, valuesForDomain(name, values)));
            domainsCollector.domain(name, values);
            return this;
        }


        public TraversalBuilder<Params, ParamsGrade> then(){
            startFromCollector.then();
            domainsCollector.then();
            return TraversalBuilder.this;
        }
    }

    public TraversalBuilder<Params, ParamsGrade> stage(TraversalStage stage){
        stages.add(stage);
        return this;
    }

    public ParameterSpaceTraverser<Params> build(){
        return new SimpleTraverser<Params, ParamsGrade>(
            initialParams,
            stages,
            evaluator
        );
    }
}
