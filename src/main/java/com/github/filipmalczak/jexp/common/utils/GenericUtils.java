package com.github.filipmalczak.jexp.common.utils;

import java.lang.reflect.ParameterizedType;

public class GenericUtils {
    public static Class<?> getGenericParameter(Class<?> parameterized, Class<?> parameterDefinition, int parameterIdx){
        while (!parameterized.getSuperclass().equals(parameterDefinition))
            parameterized = parameterized.getSuperclass();
        return (Class<?>) ((ParameterizedType) parameterized.getGenericSuperclass()).getActualTypeArguments()[parameterIdx];
    }
}
