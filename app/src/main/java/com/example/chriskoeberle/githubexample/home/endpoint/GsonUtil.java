package com.example.chriskoeberle.githubexample.home.endpoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        return builder.create();
    }
}
