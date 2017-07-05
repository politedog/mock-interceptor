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

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.functions.Func1;

public class RxEndpointsImpl implements RxEndpoints {
    private static final String USER = "users";
    private static final String GIST = "gists";

    @Override
    public Observable<User> getUser(String userName) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(USER)
                .addPathSegment(userName)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<User>(UserImpl.class));
    }

    @Override
    public Observable<Organization> getOrg(String orgName) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(USER)
                .addPathSegment(orgName)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<Organization>(OrganizationImpl.class));
    }

    @Override
    public Observable<Gist[]> getGists() {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(GIST)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<Gist[]>(GistImpl[].class));
    }

    @Override
    public Observable<Gist> getGist(String id) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(GIST)
                .addPathSegment(id)
                .build();
        return getResponse(url)
                .flatMap(new FetchString())
                .flatMap(new ToJson<Gist>(GistImpl.class));

    }

    @Override
    public Observable<Gist> createGist(Gist gist) {
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(GIST)
                .build();
        return getResponseFromPost(url, GsonUtil.getGson().toJson(gist))
                .flatMap(new FetchString())
                .flatMap(new ToJson<Gist>(GistImpl.class));
    }


    private Observable<Response> getResponse(final HttpUrl url) {
        return Observable.fromCallable(new Callable<Response>() {
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

    private Observable<Response> getResponseFromPost(final HttpUrl url, final String body) {
        return Observable.fromCallable(new Callable<Response>() {
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

    private class FetchString implements Func1<Response, Observable<String>> {
        @Override
        public Observable<String> call(final Response response) {
            return Observable.fromCallable(new Callable<String>() {
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

    private class ToJson<T>  implements Func1<String, Observable<T>> {
        private final Class mTargetClass;

        private ToJson(Class mTargetClass) {
            this.mTargetClass = mTargetClass;
        }

        @Override
        public Observable<T> call(final String s) {
            return Observable.fromCallable(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return (T) ServiceInjector.resolve(Gson.class).fromJson(s, mTargetClass);
                }
            });
        }
    }
}
