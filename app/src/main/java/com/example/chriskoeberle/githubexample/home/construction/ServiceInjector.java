package com.example.chriskoeberle.githubexample.home.construction;

public class ServiceInjector {
    public static <T> T resolve(Class<? extends T> type) {
        return ServiceLocator.get(type);
    }
}
