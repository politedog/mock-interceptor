package com.example.chriskoeberle.githubexample.home.endpoint;


import com.example.chriskoeberle.githubexample.home.construction.ServiceInjector;
import com.example.chriskoeberle.githubexample.home.construction.ServiceLocator;
import com.example.chriskoeberle.githubexample.home.model.Organization;

import org.junit.Test;

import java.util.List;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

public class OrganizationTest extends BaseApiTest {
    @Test
    public void testOrganization() {
        ServiceLocator.put(OkHttpClient.class, OkHttpClientUtil.getOkHttpClient(null, MockBehavior.MOCK));
        Observable<Organization> observable = ServiceInjector.resolve(RxEndpoints.class).getOrg("bottlerocketstudios");
        TestSubscriber<Organization> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        List<Organization> orgList = testSubscriber.getOnNextEvents();
        assertEquals(orgList.size(), 1);
        assertEquals(orgList.get(0).getName(), "Bottle Rocket Studios");
    }

}
