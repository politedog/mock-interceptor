package com.example.chriskoeberle.githubexample.home.model;

public class UserImpl extends GithubObject implements User {
    private String avatar_url;
    private String gravatar_id;
    private String login;
    private String type;
    private String name;
    private String blog;
    private String location;
    private String email;
    private String hireable;
    private String bio;
    private String company;
    private boolean site_admin;
    private int public_repos;
    private int public_gists;
    private int followers;
    private int following;

    @Override
    public String getName() {
        return name;
    }
}
