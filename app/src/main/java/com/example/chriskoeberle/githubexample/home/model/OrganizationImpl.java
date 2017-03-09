package com.example.chriskoeberle.githubexample.home.model;

public class OrganizationImpl implements Organization {
    private String login;
    private String avatar_url;
    private String description;
    private String name;
    private String company;
    private String blog;
    private String location;
    private String email;
    private String html_url;
    private String type;

    private int public_repos;
    private int public_gists;
    private int followers;
    private int following;

    @Override
    public String getName() {
        return name;
    }
}
