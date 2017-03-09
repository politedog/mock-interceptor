package com.example.chriskoeberle.githubexample.home.endpoint;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.example.chriskoeberle.githubexample.home.construction.ServiceInjector;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chris.koeberle on 3/9/17.
 */

public abstract class BaseApiAgent<ResultType> extends AbstractAgent<ResultWrapper<ResultType>, Float> {
    protected Request mRequest;
    protected boolean mCancelled;
    protected final Class<ResultType> mResultTypeClass;

    protected BaseApiAgent(Class mResultTypeClass) {
        super();
        this.mResultTypeClass = mResultTypeClass;
    }

    @Override
    public void cancel() {
        mCancelled = true;
    }

    @Override
    public void onProgressUpdateRequested() {

    }

    @Override
    public void run() {
        ResultType result = null;
        try {
            Response response = ServiceInjector.resolve(OkHttpClient.class).newCall(mRequest).execute();
            if (response.isSuccessful()) {
                String r = response.body().string();
                System.out.println(r);
                result = ServiceInjector.resolve(Gson.class).fromJson(r, mResultTypeClass);
            }
            onCompletion(response.code(), response.message(), result);
        } catch (IOException e) {
            onCompletion(0, e.getMessage(), null);
        }
    }

    private void onCompletion(int errorCode, String errorMessage, ResultType result) {
        getAgentListener().onCompletion(getUniqueIdentifier(), new ResultWrapper<ResultType>(errorCode, errorMessage, result));
    }
}
