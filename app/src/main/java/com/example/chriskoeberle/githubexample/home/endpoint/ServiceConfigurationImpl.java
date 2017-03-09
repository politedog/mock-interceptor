package com.example.chriskoeberle.githubexample.home.endpoint;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class ServiceConfigurationImpl implements ServiceConfiguration {
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VERSION = "application/vnd.github.v3+json";
    private static final String HOST = "api.github.com";
    private static final String SCHEME = "https";

    @Override
    public Request.Builder getRequestBuilder() {
        return new Request.Builder()
                .addHeader(ACCEPT, ACCEPT_VERSION);
    }

    @Override
    public HttpUrl.Builder getUrlBuilder() {
        return new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(HOST);
    }
}
