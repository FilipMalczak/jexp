package com.github.filipmalczak.jexp.api.common;

import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public interface Parametrized<Params extends Copyable<Params>> {
    void setParameters(Params parameters);
    Params getParameters();

    default void setParameters(UnresolvedParameters<Params> unresolvedParameters){
        setParameters(unresolvedParameters.resolve());
    }
}
