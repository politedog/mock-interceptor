package com.example.chriskoeberle.githubexample.home.construction;

import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {
    Map<Class<?>, Object> mLocatorMap;

    private ServiceLocator() {
        mLocatorMap = new HashMap<>();
    }

    private static class SingletonHolder {
        static final ServiceLocator instance = new ServiceLocator();
    }

    private static ServiceLocator getInstance() {
        return SingletonHolder.instance;
    }

    public static <T> void put(Class<T> type, T instance) {
        if (type == null) {
            throw new NullPointerException();
        }
        getInstance().mLocatorMap.put(type, instance);
    }

    public static <T> T get(Class<T> type) {
        return (T) getInstance().mLocatorMap.get(type);
    }
}
