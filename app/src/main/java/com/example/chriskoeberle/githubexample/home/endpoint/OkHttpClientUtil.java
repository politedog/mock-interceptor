package com.example.chriskoeberle.githubexample.home.endpoint;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpClientUtil {
    private static final long READ_TIMEOUT = 120;

    public static OkHttpClient getOkHttpClient(Context context, MockBehavior mock) {
        OkHttpClient okHttpClient = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
           if(mock != MockBehavior.DO_NOT_MOCK) {
                builder.addInterceptor(new MockedApiInterceptor(context, mock));
            }
        okHttpClient = builder
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
            /*
        if (context != null) {
            Picasso picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(okHttpClient))
                    .build();
            Picasso.setSingletonInstance(picasso);
        }*/
        return okHttpClient;
    }
}
