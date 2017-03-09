package com.example.chriskoeberle.githubexample.home.endpoint;

import com.bottlerocketstudios.groundcontrol.listener.AgentListener;

import java.util.concurrent.CountDownLatch;

/**
 * Created by chris.koeberle on 3/9/17.
 */

public abstract class TestListener<ResultType> implements AgentListener<ResultWrapper<ResultType>, Float> {

    private CountDownLatch mLatch;
    private ResultWrapper<ResultType> mResultWrapper;

    public TestListener<ResultType> withLatch(CountDownLatch latch) {
        mLatch = latch;
        return this;
    }

    @Override
    public void onCompletion(String agentIdentifier, ResultWrapper<ResultType> result) {
        if (mLatch != null) {
            mLatch.countDown();
        }
        mResultWrapper = result;
    }


    @Override
    public void onProgress(String agentIdentifier, Float progress) {

    }

    public ResultWrapper<ResultType> getResultWrapper() {
        return mResultWrapper;
    }
}
