package com.github.filipmalczak.jexp.common.experiments.traversal.builder.strategy;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.common.experiments.traversal.builder.Domain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@Slf4j
public class GreedyTraversalStrategyTest {
    @Data
    @AllArgsConstructor
    public static class X implements Copyable<X>{
        Integer a;
        String b;
        Double c;

        @Override
        public X copy() {
            return new X(a, b, c);
        }

        public int grade(){
            return (int) (100*a + b.length() - 100*c);
        }
    }

    @Value(staticConstructor = "of")
    private static class Pair{
        X x;
        int grade;
    }

    @Test
    public void exampleScenario(){
        val strategy = new GreedyTraversalStrategy<X>();
        List<Domain> domains = asList(
            new Domain("a", x -> asList(1, 2, 3)),
            new Domain("b", x -> asList("x", "yyy", "zz")),
            new Domain("c", x -> asList(4.2, 5.2, 1.0, 8.5, -2.0, 2.0))
        );
        X params = new X(1, "x", 4.2);
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        //fixme: I don't get the generics here
        List<Pair> runLog = new LinkedList<>();
        val run = strategy.<Integer>apply(domains);

        X result = run.traverseFrom(params, x -> singleThreadExecutor.submit(() -> {
//            reporter.publishEntry("Grading #", x.toString());
            int grade = x.grade();
            runLog.add(Pair.of(x, grade));
            return grade;
        }));
        List<Pair> expected = asList(
            new Pair(new X(1, "x", 4.2), -319),
            new Pair(new X(2, "x", 4.2), -219),
            new Pair(new X(3, "x", 4.2), -119),

            new Pair(new X(3, "x", 4.2), -119),
            new Pair(new X(3, "yyy", 4.2), -117),
            new Pair(new X(3, "zz", 4.2), -118),

            new Pair(new X(3, "yyy", 4.2), -117),
            new Pair(new X(3, "yyy", 5.2), -217),
            new Pair(new X(3, "yyy", 1.0), 203),
            new Pair(new X(3, "yyy", 8.5), -547),
            new Pair(new X(3, "yyy", -2.0), 503),
            new Pair(new X(3, "yyy", 2.0), 103)
        );
        assertEquals(expected, runLog);
        assertEquals(new X(3, "yyy", -2.0), result);
    }
}