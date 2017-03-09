package com.example.chriskoeberle.githubexample.home.endpoint;

import com.example.chriskoeberle.githubexample.home.construction.ServiceLocator;
import com.google.gson.Gson;

import org.junit.Before;

public class BaseApiTest {
    @Before
    public void setup() {
        ServiceLocator.put(RxEndpoints.class, new RxEndpointsImpl());
        ServiceLocator.put(ServiceConfiguration.class, new ServiceConfigurationImpl());
        ServiceLocator.put(Gson.class, GsonUtil.getGson());
    }
}
