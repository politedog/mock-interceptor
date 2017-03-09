package com.example.chriskoeberle.githubexample.home.endpoint;

/**
 * Created by chris.koeberle on 3/9/17.
 */
public class ResultWrapper<T> {
    private final T mResult;
    private final String mErrorMessage;
    private final int mErrorCode;

    public ResultWrapper(int errorCode, String errorMessage, T result) {
        this.mResult = result;
        this.mErrorCode = errorCode;
        this.mErrorMessage = errorMessage;
    }

    public T getResult() {
        return mResult;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

}
