package com.example.chriskoeberle.githubexample.home.model;

/**
 * Created by chris.koeberle on 3/9/17.
 */

public interface GistFile {
    int getSize();
    String getFilename();
    String getContent();

    void setContent(String content);
}
