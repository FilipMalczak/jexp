package com.github.filipmalczak.jexp.api.experiments.traversal.listener;

import com.github.filipmalczak.jexp.api.common.Copyable;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public class NoOpListener<Params extends Copyable<Params>> implements TraversalListener<Params> {
    @Override
    public void onNewParameters(Params newParams) {

    }
}
