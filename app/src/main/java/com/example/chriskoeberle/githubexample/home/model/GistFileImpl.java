package com.example.chriskoeberle.githubexample.home.model;

/**
 * Created by chris.koeberle on 3/9/17.
 */
public class GistFileImpl implements GistFile {
    private String content;
    private String filename;
    private String language;
    private String raw_url;
    private String type;
    private int size;
    private boolean truncated;

    public GistFileImpl(String content) {
        setContent(content);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }
}
