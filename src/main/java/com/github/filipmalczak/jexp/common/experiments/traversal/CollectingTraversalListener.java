package com.github.filipmalczak.jexp.common.experiments.traversal;

import com.github.filipmalczak.jexp.api.common.Copyable;
import com.github.filipmalczak.jexp.api.experiments.traversal.listener.TraversalListener;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CollectingTraversalListener<Params extends Copyable<Params>> implements TraversalListener<Params> {
    @Data
    @AllArgsConstructor
    public static class TraversalHistory<Params> implements Copyable<TraversalHistory<Params>>{
        private Params initialParameters;
        private List<Params> newParametersHistory;
        private Params finalParameters;

        @Override
        public TraversalHistory<Params> copy() {
            return new TraversalHistory<>(initialParameters, new ArrayList(newParametersHistory), finalParameters);
        }
    }

    public final TraversalHistory<Params> history = new TraversalHistory<>(null, new LinkedList<>(), null);

    @Override
    public void beforeTraversal(Params initialParams) {
        history.initialParameters = initialParams;
    }

    @Override
    public void onGradingParameters(Params newParams) {
        history.newParametersHistory.add(newParams);
    }

    @Override
    public void onTraversalEnd(Params finalParams) {
        history.finalParameters = finalParams;
    }
}
