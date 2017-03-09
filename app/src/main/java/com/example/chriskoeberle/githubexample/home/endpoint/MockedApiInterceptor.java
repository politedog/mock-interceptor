package com.example.chriskoeberle.githubexample.home.endpoint;

import android.content.Context;

public class MockedApiInterceptor extends AbstractMockedApiInterceptor {

    public MockedApiInterceptor(Context context, MockBehavior mock) {
        super(context, mock);
        addSpecs();
    }

    private void addSpecs() {
        mResponseList.add(new ResponseSpec("/gists", "gists"));
        mResponseList.add(new ResponseSpec("/gists/89dfa1710ace6b2aa67254271b487892", "89dfa1710ace6b2aa67254271b487892"));
        mResponseList.add(new ResponseSpec("/gists/3d7cbc2f66cf5d61b8014d957a270c7c", "3d7cbc2f66cf5d61b8014d957a270c7c"));
        mResponseList.add(new ResponseSpec("/gists/not%20actually%20an%20ID", "not%20actually%20an%20ID-404").setCode(404).setMessage("Not Found"));
        mResponseList.add(new ResponseSpec("/users/bottlerocketstudios", "bottlerocketstudios"));
        mResponseList.add(new ResponseSpec("/users/bottlerocketapps", "bottlerocketapps"));
        mResponseList.add(new ResponseSpec("/gists", "gists-201-POST-806811052").setCode(201).setMessage("Created").setMethod("POST").addBodyContains("AbstractMockedInterceptor"));
        mResponseList.add(new ResponseSpec("/gists/19c6aadff4c74dbeb108ec21003c7822", "19c6aadff4c74dbeb108ec21003c7822"));
    }


}
