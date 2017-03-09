package com.example.chriskoeberle.githubexample.home.endpoint;

import okhttp3.HttpUrl;
import okhttp3.Request;

public interface ServiceConfiguration {
    Request.Builder getRequestBuilder();
    HttpUrl.Builder getUrlBuilder();
}
