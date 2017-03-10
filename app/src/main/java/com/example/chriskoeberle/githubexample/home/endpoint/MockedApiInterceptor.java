package com.example.chriskoeberle.githubexample.home.endpoint;

import android.content.Context;

public class MockedApiInterceptor extends AbstractMockedApiInterceptor {

    public MockedApiInterceptor(Context context, MockBehavior mock) {
        super(context, mock);
        addSpecs();
    }

    private void addSpecs() {
        mResponseList.add(new NetworkCallSpec("/gists", "gists"));
        mResponseList.add(new NetworkCallSpec("/gists/89dfa1710ace6b2aa67254271b487892", "89dfa1710ace6b2aa67254271b487892"));
        mResponseList.add(new NetworkCallSpec("/gists/3d7cbc2f66cf5d61b8014d957a270c7c", "3d7cbc2f66cf5d61b8014d957a270c7c"));
        mResponseList.add(new NetworkCallSpec("/gists/not%20actually%20an%20ID", "not%20actually%20an%20ID-404").setRequestCode(404).setResponseMessage("Not Found"));
        mResponseList.add(new NetworkCallSpec("/users/bottlerocketstudios", "bottlerocketstudios"));
        mResponseList.add(new NetworkCallSpec("/users/bottlerocketapps", "bottlerocketapps"));
        mResponseList.add(new NetworkCallSpec("/gists", "gists-201-POST-806811052").setRequestCode(201).setResponseMessage("Created").setRequestMethod("POST").addRequestBodyContains("AbstractMockedInterceptor"));
        mResponseList.add(new NetworkCallSpec("/gists/19c6aadff4c74dbeb108ec21003c7822", "19c6aadff4c74dbeb108ec21003c7822"));
    }


}
