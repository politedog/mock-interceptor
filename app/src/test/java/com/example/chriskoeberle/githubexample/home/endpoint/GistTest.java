package com.example.chriskoeberle.githubexample.home.endpoint;

import com.example.chriskoeberle.githubexample.home.construction.ServiceInjector;
import com.example.chriskoeberle.githubexample.home.construction.ServiceLocator;
import com.example.chriskoeberle.githubexample.home.model.Gist;
import com.example.chriskoeberle.githubexample.home.model.GistFile;
import com.example.chriskoeberle.githubexample.home.model.GistImpl;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

public class GistTest extends BaseApiTest {
    @Test
    public void testAllGists() {
        ServiceLocator.put(OkHttpClient.class, OkHttpClientUtil.getOkHttpClient(null, MockBehavior.MOCK));
        Observable<Gist[]> observable = ServiceInjector.resolve(RxEndpoints.class).getGists();
        TestSubscriber<Gist[]> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        List<Gist[]> gists = testSubscriber.getOnNextEvents();
        Gist gist = gists.get(0)[0];
        Observable<Gist> gistObservable = ServiceInjector.resolve(RxEndpoints.class).getGist(gist.getId());
        TestSubscriber<Gist> gistTestSubscriber = new TestSubscriber<>();
        gistObservable.subscribe(gistTestSubscriber);
        Gist detailGist = gistTestSubscriber.getOnNextEvents().get(0);
        assertEquals(detailGist.getDescription(), gist.getDescription());
    }

    @Test
    public void testOneGist() {
        ServiceLocator.put(OkHttpClient.class, OkHttpClientUtil.getOkHttpClient(null, MockBehavior.MOCK));
        Observable<Gist> observable = ServiceInjector.resolve(RxEndpoints.class).getGist("3d7cbc2f66cf5d61b8014d957a270c7c");
        TestSubscriber<Gist> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        List<Gist> gistList = testSubscriber.getOnNextEvents();
        Gist gist = gistList.get(0);
        assertEquals("Bootstrap Customizer Config", gist.getDescription());
        GistFile file = gist.getFile(gist.getFilenames().iterator().next());
        assertEquals(file.getContent().length(), file.getSize());
        assertEquals("config.json", file.getFilename());
        observable = ServiceInjector.resolve(RxEndpoints.class).getGist("not actually an ID");
        testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoValues();
        List<Throwable> errorList = testSubscriber.getOnErrorEvents();
        assertEquals(errorList.size(), 1);
        assertEquals("Not Found", errorList.get(0).getMessage());
    }

    private static final String CREATE_FILE_NAME = "AbstractMockedInterceptor.java";
    private static final String CREATE_DESCRIPTION = "An OkHttp Interceptor that returns mocked results if it has them.";
    @Test
    public void createGist() throws IOException {
        ServiceLocator.put(OkHttpClient.class, OkHttpClientUtil.getOkHttpClient(null, MockBehavior.MOCK_ONLY));
        Gist gist = new GistImpl();
        gist.setDescription(CREATE_DESCRIPTION);
        gist.addFile(CREATE_FILE_NAME, readFromAsset("mocks/javaclass"));
        Observable<Gist> observable = ServiceInjector.resolve(RxEndpoints.class).createGist(gist);
        TestSubscriber<Gist> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        List<Gist> gistList = testSubscriber.getOnNextEvents();
        Gist resultGist = gistList.get(0);
        Observable<Gist> gistObservable = ServiceInjector.resolve(RxEndpoints.class).getGist(resultGist.getId());
        TestSubscriber<Gist> gistTestSubscriber = new TestSubscriber<>();
        gistObservable.subscribe(gistTestSubscriber);
        Gist detailGist = gistTestSubscriber.getOnNextEvents().get(0);
        assertEquals(detailGist.getDescription(), resultGist.getDescription());
    }

    protected String readFromAsset(String filename) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        if (stream==null){
            System.out.println("No stream for "+filename+"!");
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}

