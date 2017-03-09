package com.example.chriskoeberle.githubexample.home.model;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class GistImpl extends GithubObject implements Gist {
    private String url;
    private String description;
    private String user;
    @SerializedName("public")
    private boolean isPublic;
    private boolean truncated;
    private int comments;

    HashMap<String, GistFileImpl> files;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getFilenames() {
        return files.keySet();
    }

    @Override
    public GistFile getFile(String filename) {
        return files.get(filename);
    }

    @Override
    public void addFile(String filename, String content) {
        if (files == null) {
            files = new HashMap<>();
        }
        files.put(filename, new GistFileImpl(content));
    }

    @Override
    public void setDescription(String description) {

    }
}
