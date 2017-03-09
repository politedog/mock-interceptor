package com.example.chriskoeberle.githubexample.home.model;

import java.util.Set;

public interface Gist {
    public String getDescription();
    public String getId();

    public Set<String> getFilenames();
    public GistFile getFile(String filename);
    public void addFile(String filename, String content);
    public void setDescription(String description);
}
