package com.github.filipmalczak.jexp.api.common;


import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apiguardian.api.API;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@UtilityClass
@API(status = API.Status.INTERNAL)
public class PropertySupport {
    //todo: add support for boxing and unboxing

    private Method findSetter(Class<?> clazz, String propName, Class<?> valueType) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(setterName(propName), new Class[]{valueType});
    }

    private Method findGetter(Class<?> clazz, String propName) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(getterName(propName), new Class[0]);
    }

    private Field findField(Class<?> clazz, String propName) throws NoSuchFieldException {
        Field out = clazz.getDeclaredField(propName);
        out.setAccessible(true);
        return out;
    }

    private String accessorName(String prefix, String propName){
        return prefix + propName.substring(0, 1).toUpperCase() + propName.substring(1);
    }

    private String setterName(String propName){
        return accessorName("set", propName);
    }

    private String getterName(String propName){
        return accessorName("get", propName);
    }

    @SneakyThrows
    public void setProperty(Object target, String propName, Object value){
        Class targetClass = target.getClass();
        Class valueClass = value.getClass();
        try {
            Method setter = findSetter(targetClass, propName, valueClass);
            setter.invoke(target, value);
        } catch (NoSuchMethodException e){
            Field field = findField(targetClass, propName);
            field.set(target, value);
        }
    }

    @SneakyThrows
    public Object getProperty(Object target, String propName){
        Class targetClass = target.getClass();
        try {
            Method setter = findGetter(targetClass, propName);
            return setter.invoke(target);
        } catch (NoSuchMethodException e){
            Field field = findField(targetClass, propName);
            return field.get(target);
        }
    }
}
