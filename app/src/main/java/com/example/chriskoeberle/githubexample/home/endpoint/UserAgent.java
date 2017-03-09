package com.example.chriskoeberle.githubexample.home.endpoint;

import com.example.chriskoeberle.githubexample.home.construction.ServiceInjector;
import com.example.chriskoeberle.githubexample.home.model.User;
import com.example.chriskoeberle.githubexample.home.model.UserImpl;

import okhttp3.HttpUrl;

/**
 * Created by chris.koeberle on 3/9/17.
 */

public class UserAgent extends BaseApiAgent<User> {
    private static final String USER = "users";
    private final String mUniqId;

    protected UserAgent(String userName) {
        super(UserImpl.class);
        mUniqId = userName;
        HttpUrl url = ServiceInjector.resolve(ServiceConfiguration.class).getUrlBuilder()
                .addPathSegment(USER)
                .addPathSegment(userName)
                .build();
        mRequest = ServiceInjector.resolve(ServiceConfiguration.class).getRequestBuilder()
                .url(url)
                .build();
    }

    @Override
    public String getUniqueIdentifier() {
        return mUniqId;
    }
}
