package com.example.chriskoeberle.githubexample.home.endpoint;


import com.example.chriskoeberle.githubexample.home.model.Gist;
import com.example.chriskoeberle.githubexample.home.model.Organization;
import com.example.chriskoeberle.githubexample.home.model.User;

import rx.Observable;

public interface RxEndpoints {
    Observable<User> getUser(String userName);
    Observable<Organization> getOrg(String orgName);
    Observable<Gist[]> getGists();
    Observable<Gist> getGist(String id);
    Observable<Gist> createGist(Gist gist);
}
