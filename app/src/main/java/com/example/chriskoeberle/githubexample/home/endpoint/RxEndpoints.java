package com.example.chriskoeberle.githubexample.home.endpoint;


import com.example.chriskoeberle.githubexample.home.model.Gist;
import com.example.chriskoeberle.githubexample.home.model.Organization;
import com.example.chriskoeberle.githubexample.home.model.User;

import io.reactivex.Flowable;

public interface RxEndpoints {
    Flowable<User> getUser(String userName);
    Flowable<Organization> getOrg(String orgName);
    Flowable<Gist[]> getGists();
    Flowable<Gist> getGist(String id);
    Flowable<Gist> createGist(Gist gist);
}
