package com.github.filipmalczak.jexp.api.common;

import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public interface Copyable<This extends Copyable<This>> {
    This copy();
}
