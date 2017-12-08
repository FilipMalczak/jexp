package com.github.filipmalczak.jexp.api.experiments.traversal.listener;

import com.github.filipmalczak.jexp.api.common.Copyable;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface TraversalListener<Params extends Copyable<Params>> {
    default void beforeTraversal(Params initialParams){}

    //todo: add grade to this?
    void onGradingParameters(Params newParams);

    default void onTraversalEnd(Params finalParams){}
}
