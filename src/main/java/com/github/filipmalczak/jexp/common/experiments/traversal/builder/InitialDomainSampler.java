package com.github.filipmalczak.jexp.common.experiments.traversal.builder;

import java.util.List;

@FunctionalInterface
public interface InitialDomainSampler<V> {
    V chooseInitialValue(String domainName, List<V> domainValues);

    public static <V> InitialDomainSampler<V> first(){
        return (n, v) -> v.get(0);
    }

    public static <V> InitialDomainSampler<V> last(){
        return (n, v) -> v.get(v.size()-1);
    }

    public static <V> InitialDomainSampler<V> middle(){
        return (n, v) -> v.get(v.size()/2);
    }
}
