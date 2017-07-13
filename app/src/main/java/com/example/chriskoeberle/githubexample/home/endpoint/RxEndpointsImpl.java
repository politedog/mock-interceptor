package com.example.chriskoeberle.githubexample.home.endpoint;

import com.example.chriskoeberle.githubexample.home.construction.ServiceInjector;
import com.example.chriskoeberle.githubexample.home.model.Gist;
import com.example.chriskoeberle.githubexample.home.model.GistImpl;
import com.example.chriskoeberle.githubexample.home.model.Organization;
import com.example.chriskoeberle.githubexample.home.model.OrganizationImpl;
import com.example.chriskoeberle.githubexample.home.model.User;
import com.example.chriskoeberle.githubexample.home.model.UserImpl;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RxEndpointsImpl implements RxEndpoints {
    private static final String USER = "users";
    private static final String GIST = "gists";

    @Override
    public Flowable<User> getUser(String userName) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(USER)
                .addPathSegment(userName)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<User>(UserImpl.class));
    }

    @Override
    public Flowable<Organization> getOrg(String orgName) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(USER)
                .addPathSegment(orgName)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<Organization>(OrganizationImpl.class));
    }

    @Override
    public Flowable<Gist[]> getGists() {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(GIST)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<Gist[]>(GistImpl[].class));
    }

    @Override
    public Flowable<Gist> getGist(String id) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(GIST)
                .addPathSegment(id)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<Gist>(GistImpl.class));

    }

    @Override
    public Flowable<Gist> createGist(Gist gist) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(GIST)
                .build();
        return getResponseFromPost(url, GsonUtil.getGson().toJson(gist))
                .flatMap(new FetchString())
                .flatMap(new ToJson<Gist>(GistImpl.class));
    }


    private Flowable<Response> getResponse(final HttpUrl url) {
        return Flowable.fromCallable(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                System.out.println(url);
                Request request = ServiceInjector.resolve(ServiceConfiguration.class).getRequestBuilder()
                        .url(url)
                        .build();
                return ServiceInjector.resolve(OkHttpClient.class).newCall(request).execute();
            }
        });
    }

    private Flowable<Response> getResponseFromPost(final HttpUrl url, final String body) {
        return Flowable.fromCallable(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                System.out.println(url);
                Request request = ServiceInjector.resolve(ServiceConfiguration.class).getRequestBuilder()
                        .post(RequestBody.create(MediaType.parse("application/json"), body))
                        .url(url)
                        .build();
                return ServiceInjector.resolve(OkHttpClient.class).newCall(request).execute();
            }
        });
    }

    private class FetchString implements Function<Response, Flowable<String>> {
        @Override
        public Flowable<String> apply(final Response response) {
            return Flowable.fromCallable(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    if (!response.isSuccessful()) {
                        throw new IOException(response.message());
                    }
                    String responseString = response.body().string();
                    System.out.println(responseString);
                    return responseString;
                }
            });
        }
    }

    private class ToJson<T>  implements Function<String, Flowable<T>> {
        private final Class mTargetClass;

        private ToJson(Class mTargetClass) {
            this.mTargetClass = mTargetClass;
        }

        @Override
        public Flowable<T> apply(final String s) {
            return Flowable.fromCallable(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return (T) ServiceInjector.resolve(Gson.class).fromJson(s, mTargetClass);
                }
            });
        }
    }
}
