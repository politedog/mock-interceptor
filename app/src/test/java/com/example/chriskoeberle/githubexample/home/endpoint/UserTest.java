package com.example.chriskoeberle.githubexample.home.endpoint;


import com.example.chriskoeberle.githubexample.home.construction.ServiceInjector;
import com.example.chriskoeberle.githubexample.home.construction.ServiceLocator;
import com.example.chriskoeberle.githubexample.home.model.User;

import org.junit.Test;

import java.util.List;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

public class UserTest extends BaseApiTest {

    @Test
    public void testUser() {
        ServiceLocator.put(OkHttpClient.class, OkHttpClientUtil.getOkHttpClient(null, MockBehavior.MOCK));
        Observable<User> observable = ServiceInjector.resolve(RxEndpoints.class).getUser("bottlerocketapps");
        TestSubscriber<User> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        List<User> userList = testSubscriber.getOnNextEvents();
        assertEquals(userList.size(), 1);
        assertEquals(userList.get(0).getName(), "Bottle Rocket");

    }
}
