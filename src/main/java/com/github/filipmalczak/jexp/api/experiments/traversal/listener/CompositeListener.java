package com.github.filipmalczak.jexp.api.experiments.traversal.listener;

import com.github.filipmalczak.jexp.api.common.Copyable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@API(status = API.Status.EXPERIMENTAL)
public class CompositeListener<Params extends Copyable<Params>> implements TraversalListener<Params> {
    //this is probably the only place where Guavas newArrayList() would be used
    private List<TraversalListener<Params>> components = new ArrayList<>();

    public CompositeListener(TraversalListener<Params>... listeners){
        components = Arrays.asList(listeners);
    }

    @Override
    public void beforeTraversal(Params initialParams) {
        components.stream().forEach(c -> c.beforeTraversal(initialParams));
    }

    @Override
    public void onGradingParameters(Params newParams) {
        components.stream().forEach(c -> c.onGradingParameters(newParams));
    }

    @Override
    public void onTraversalEnd(Params finalParams) {
        components.stream().forEach(c -> c.onTraversalEnd(finalParams));
    }
}
