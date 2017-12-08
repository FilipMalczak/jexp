package com.github.filipmalczak.jexp.common.experiments.traversal.builder;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.evaluation.ParametersEvaluator;
import com.github.filipmalczak.jexp.api.experiments.traversal.ParameterSpaceTraverser;
import com.github.filipmalczak.jexp.common.experiments.traversal.CollectingTraversalListener;
import com.github.filipmalczak.jexp.common.experiments.traversal.builder.strategy.GreedyTraversalStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDate;

import static java.util.Arrays.asList;

@Slf4j
public class TraversalBuilderTest {
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    static class ParamsExample implements Copyable<ParamsExample>{
        String a;
        @Accessors int b;
        LocalDate c;

        @Override
        public ParamsExample copy() {
            return new ParamsExample(a, b, c);
        }
    }

    @Test
    public void exampleScenario(){
        TraversalBuilder<ParamsExample, Integer> builder = TraversalBuilder.getNew(ParamsExample.class);
        ParameterSpaceTraverser<ParamsExample> traverser = builder.
            using().
                evaluator(ParametersEvaluator.of(pe -> pe.a.length() + pe.b + pe.c.getMonth().getValue())).
                parameter("c", (String s) -> LocalDate.parse(s)).
            then().
            startFrom().
                valueOf("a", "XYZ").
                valueOf("b", 3).
                valueOf("c", "2017-05-15").
            then().
            withStrategy(new GreedyTraversalStrategy<>()).
                domain("a", asList("XYZ", "A", "DEFGH")).
                domain("c", asList("2017-05-15", "2017-05-16", "2017-06-18")).
            then().
        build();
        CollectingTraversalListener<ParamsExample> listener = new CollectingTraversalListener<>();
        traverser.traverse(listener);
        CollectingTraversalListener.TraversalHistory<ParamsExample> runLog = listener.history;
        CollectingTraversalListener.TraversalHistory<ParamsExample> expected = new CollectingTraversalListener.TraversalHistory<>(
            new ParamsExample("XYZ", 3, LocalDate.parse("2017-05-15")),
            asList(
                new ParamsExample("XYZ", 3, LocalDate.parse("2017-05-15")),
                new ParamsExample("A", 3, LocalDate.parse("2017-05-15")),
                new ParamsExample("DEFGH", 3, LocalDate.parse("2017-05-15")),
                new ParamsExample("DEFGH", 3, LocalDate.parse("2017-05-15")),
                new ParamsExample("DEFGH", 3, LocalDate.parse("2017-05-16")),
                new ParamsExample("DEFGH", 3, LocalDate.parse("2017-06-18"))
            ),
            new ParamsExample("DEFGH", 3, LocalDate.parse("2017-06-18"))
        );
        log.info(runLog.toString());
    }
}
