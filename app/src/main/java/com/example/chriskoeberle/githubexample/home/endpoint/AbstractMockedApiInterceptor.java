package com.example.chriskoeberle.githubexample.home.endpoint;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public abstract class AbstractMockedApiInterceptor implements Interceptor {
    private static final String OFFSET_PARAMETER = "offset";
    private static final String INDEX_PARAMETER = "index";
    private static final String NAME_PARAMETER = "name";
    private static final Pattern DATETIME = Pattern.compile("%DATETIME[^%]*%");
    private static final Pattern DATE = Pattern.compile("%DATE[^%]*%");
    private static final Pattern PARAMETER = Pattern.compile("%PARAMETER[^%]*%");

    private final Context mContext;
    private final MockBehavior mMockBehavior;

    protected final ArrayList<NetworkCallSpec> mResponseList = new ArrayList<>();
    private ArrayList<StringSubstitutor> mSubstitutorList;

    public static class NetworkCallSpec {
        private final String mResponseFilename;
        private int mResponseCode;
        private String mResponseMessage;
        private final String mRequestUrlPattern;
        private String mRequestMethod;
        private Map<String, String> mRequestQueryParameters;
        private String mRequestBody;
        private Set<String> mRequestBodyContains;

        protected NetworkCallSpec(String requestPattern, String responseFilename) {
            this.mRequestUrlPattern = requestPattern;
            this.mResponseFilename = responseFilename;
            this.mResponseCode = HttpURLConnection.HTTP_OK;
            this.mResponseMessage = "OK";
            this.mRequestQueryParameters = new HashMap<>();
            this.mRequestMethod = "GET";
        }

        public NetworkCallSpec setRequestCode(int code) {
            mResponseCode = code;
            return this;
        }

        public NetworkCallSpec setRequestMethod(String method) {
            mRequestMethod = method;
            return this;
        }

        public NetworkCallSpec setResponseMessage(String message) {
            this.mResponseMessage = message;
            return this;
        }

        public NetworkCallSpec addRequestQueryParameter(String keyPattern, String valuePattern) {
            this.mRequestQueryParameters.put(keyPattern, valuePattern);
            return this;
        }

        public NetworkCallSpec addRequestBody(String body) {
            if ("GET".equalsIgnoreCase(this.mRequestMethod)) {
                this.mRequestMethod = "POST";
            }
            this.mRequestBody = body;
            return this;
        }

        public NetworkCallSpec addRequestBodyContains(String bodyBit) {
            if ("GET".equalsIgnoreCase(this.mRequestMethod)) {
                this.mRequestMethod = "POST";
            }
            if (mRequestBodyContains == null) {
                mRequestBodyContains = new HashSet<>();
            }
            mRequestBodyContains.add(bodyBit);
            return this;
        }

        public boolean matches(HttpUrl url, String method, String body) {
            if (!url.encodedPath().matches(mRequestUrlPattern)) {
                return false;
            }
            if (!mRequestMethod.equalsIgnoreCase(method)) {
                return false;
            }
            if (mRequestMethod.equalsIgnoreCase("POST") && !TextUtils.isEmpty(mRequestBody) && !mRequestBody.equalsIgnoreCase(body)) {
                return false;
            }
            if (mRequestMethod.equalsIgnoreCase("POST") && mRequestBodyContains != null) {
                for (String contains : mRequestBodyContains) {
                    if (!body.contains(contains)) {
                        return false;
                    }
                }
            }
            for (Map.Entry<String, String> kvp : mRequestQueryParameters.entrySet()) {
                boolean foundKey = false;
                boolean foundValue = false;
                for (String key : url.queryParameterNames()) {
                    if (key.matches(kvp.getKey())) {
                        foundKey = true;
                        String value = url.queryParameter(key);
                        if (value != null && value.matches(kvp.getValue())) {
                            foundValue = true;
                        }
                        if (value == null && (kvp.getValue() == null || kvp.getValue() == "" || kvp.getValue().equalsIgnoreCase("null"))) {
                            foundValue = true;
                        }
                    }
                }
                if (!foundKey || !foundValue) {
                    return false;
                }
            }
            return true;
        }
    }

    public AbstractMockedApiInterceptor(Context context, MockBehavior mock) {
        if (context != null) {
            mContext = context.getApplicationContext();
        } else {
            mContext = null;
        }
        mMockBehavior = mock;
        addSubstitutors();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        NetworkCallSpec mockFound = null;
        for (NetworkCallSpec spec : mResponseList) {
            if (spec.matches(request.url(), request.method(), stringifyRequestBody(request))) {
                mockFound = spec;
                Response.Builder builder = new Response.Builder();
                String bodyString = resolveAsset("mocks/"+spec.mResponseFilename);
                bodyString = substituteStrings(bodyString, request);
                if(bodyString != null) {
                    ResponseBody body = ResponseBody.create(MediaType.parse(getMockedMediaType()), bodyString);
                    builder = builder.body(body).request(request).protocol(Protocol.HTTP_1_1).code(spec.mResponseCode).message(spec.mResponseMessage);
                }
                if (!ignoreExistingMocks()) {
                    noteThatThisFileWasUsed(spec.mResponseFilename);
                    return builder.build();
                }

            }
        }
        if (fetchNetworkResults()) {
            Response response = chain.proceed(request);
            if (!request.url().encodedPath().endsWith("png")
                    && !request.url().encodedPath().endsWith("jpg")) {
                response = memorializeRequest(request, response, mockFound);
            }
            return response;
        }
        throw new IOException("Unable to handle request with current mocking strategy");
    }

    protected boolean fetchNetworkResults() { return mMockBehavior != MockBehavior.MOCK_ONLY; }

    protected boolean ignoreExistingMocks() {
        return mMockBehavior == MockBehavior.LOG_ONLY;
    }

    protected String getMockedMediaType() {
        return "json";
    }

    private String substituteStrings(String bodyString, Request request) {
        // Because each match can get replaced with something different, we have to reset the matcher after every replacement.
        // This way of doing things happens to enforce this in a non-obvious way, because we create a new matcher every time.
        for (StringSubstitutor substitutor : mSubstitutorList) {
            while (substitutor.matchesFound(bodyString)) {
                bodyString = substitutor.replaceOneString(bodyString, request);
            }
        }
        return bodyString;
    }

    private static Map<String, String> getQueryFromUri(String match) {
        if (!match.contains("?")) {
            return new HashMap<>();
        }
        String queryString= match.substring(match.indexOf("?")+1, match.length()-1);
        Map<String, String> query = queryStringToNamesAndValues(queryString);
        return query;
    }

    private Response memorializeRequest(Request request, Response response, NetworkCallSpec mockFound) {
        Response.Builder newResponseBuilder = response.newBuilder();
        try {
            String responseString = response.body().string();
            List<String> segments = request.url().encodedPathSegments();
            String endpointName = segments.get(segments.size() - 1);
            String callSpecString = "mResponseList.add(new NetworkCallSpec(\""+request.url().encodedPath()+"\", \"::REPLACE_ME::\")";
            if (response.code() != HttpURLConnection.HTTP_OK) {
                callSpecString += ".setRequestCode("+response.code()+")";
                endpointName += "-"+response.code();
            }
            if (!TextUtils.isEmpty(response.message()) && !response.message().equalsIgnoreCase("OK")) {
                callSpecString += ".setResponseMessage(\""+response.message()+"\")";
            }
            if (!request.method().equalsIgnoreCase("GET")) {
                callSpecString += ".setRequestMethod(\""+request.method()+"\")";
                endpointName += "-"+request.method();
            }
            if (request.url().querySize()>0) {
                for (String key : request.url().queryParameterNames()) {
                    callSpecString += ".addRequestQueryParameter(\""+key.replace("[", "\\\\[").replace("]", "\\\\]")+"\", \""+request.url().queryParameter(key)+"\")";
                }
            }
            String body = stringifyRequestBody(request);
            if (body != null) {
                callSpecString += ".addRequestBody(\""+body.replace("\"", "\\\"").replace("\\u003d", "\\\\u003d")+"\")";
                endpointName += "-"+body.hashCode();
            }
            callSpecString += ");";
            if (endpointName.length()>100) {
                endpointName = ""+endpointName.hashCode();
            }
            endpointName = getUniqueName(endpointName);
            callSpecString = callSpecString.replace("::REPLACE_ME::", endpointName);
            if (mockFound != null) {
                callSpecString += " // duplicate of existing mock "+mockFound.mRequestUrlPattern;
                if (!TextUtils.isEmpty(mockFound.mRequestBody)) {
                    callSpecString += " with body "+mockFound.mRequestBody;
                }
            }
            callSpecString += "\n";
            writeToFile(callSpecString, responseString, endpointName);
            newResponseBuilder.body(ResponseBody.create(response.body().contentType(), responseString));
        } catch (IOException e) {
            Timber.e("Unable to save request to "+request.url().toString()+" : ", e);
        }
        return newResponseBuilder.build();
    }

    private String getUniqueName(String endpointName) {
        List<Integer> usedNumbers = new ArrayList<>();
        Pattern pattern = Pattern.compile(endpointName + "-\\d*$");
        List<String> possibleCollisions = new ArrayList<>();
        for (NetworkCallSpec spec : mResponseList) {
            possibleCollisions.add(spec.mResponseFilename);
        }
        for (String filename : getFilesDir().list()) {
            possibleCollisions.add(filename);
        }
        boolean collides = false;
        for (String string : possibleCollisions) {
            if (string.equalsIgnoreCase(endpointName)) {
                collides = true;
            } else {
                if (pattern.matcher(string).find()) {
                    String suffix = string.substring(string.lastIndexOf("-")+1);
                    Integer number = Integer.parseInt(suffix);
                    usedNumbers.add(number);
                }
            }
        }
        if (collides) {
            Integer i = 1;
            while (usedNumbers.contains(i)) {
                i = i + 1;
            }
            endpointName = endpointName + "-" + i;
        }
        return endpointName;
    }

    private static String stringifyRequestBody(Request request) {
        try {
            final Request copy = request.newBuilder().build();
            if (copy.body() == null) {
                return null;
            }
            final okio.Buffer buffer = new okio.Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    private void noteThatThisFileWasUsed(String filename) throws IOException {
        File dir = getFilesDir();
        if(dir == null) {
            Timber.e("Unable to access external files directory");
            return;
        }
        String filesDir = dir.getAbsolutePath();
        File specs = new File(filesDir + "/MOCKSused");
        if (!specs.exists()) {
            if (!specs.createNewFile()) {
                Timber.e("Could not create file "+specs.getAbsolutePath());
            }
        }
        FileWriter write = new FileWriter(specs, true);
        BufferedWriter writer = new BufferedWriter(write);
        writer.write(filename+"\n");
        writer.close();
    }

    private void writeToFile(String requestSpecString, String responseString, String endpoint) throws IOException {
        File dir = getFilesDir();
        if(dir == null) {
            Timber.e("Unable to access external files directory");
            return;
        }
        String filesDir = dir.getAbsolutePath();
        File specs = new File(filesDir + "/MOCKSspecs");
        if (!specs.exists()) {
            if (!specs.createNewFile()) {
                Timber.e("Could not create file "+specs.getAbsolutePath());
            }
        }
        FileWriter write = new FileWriter(specs, true);
        BufferedWriter writer = new BufferedWriter(write);
        writer.write(requestSpecString);
        writer.close();
        write = new FileWriter(filesDir + "/" + endpoint);
        writer = new BufferedWriter(write);
        writer.write(responseString);
        writer.close();
    }

    private File getFilesDir() {
        File dir = null;
        if (mContext != null) {
            dir = mContext.getExternalFilesDir(null);
        } else {
            dir = new File("mocks");
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
        return dir;
    }

    private String resolveAsset(String filename) {
        if (mContext != null) {
            return getAssetAsString(mContext, filename);
        } else {
            try {
                return readFromAsset(filename);
            } catch (IOException e) {
                Timber.e(e, "Error reading from asset - this should only be called in tests.");
            }
        }
        return null;
    }

    public static String getAssetAsString(Context context, String assetName) {
        if (context == null) {
            return null;
        }

        InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        Reader reader=null;

        try {
            AssetManager assets = context.getAssets();
            is = assets.open(assetName);

            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            Timber.e(e, "Unsupported encoding of asset: ");
            return null;
        } catch (IOException e) {
            Timber.e(e, "IOException when retrieving asset: ");
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException when closing stream: ");
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException when closing reader: ");
                }
            }
        }

        return writer.toString();
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

    static Map<String, String> queryStringToNamesAndValues(String encodedQuery) {
        Map<String, String> result = new HashMap<>();
        for (int pos = 0; pos <= encodedQuery.length(); ) {
            int ampersandOffset = encodedQuery.indexOf('&', pos);
            if (ampersandOffset == -1) ampersandOffset = encodedQuery.length();

            int equalsOffset = encodedQuery.indexOf('=', pos);
            if (equalsOffset == -1 || equalsOffset > ampersandOffset) {
                result.put(encodedQuery.substring(pos, ampersandOffset), null);
            } else {
                result.put(encodedQuery.substring(pos, equalsOffset), (encodedQuery.substring(equalsOffset + 1, ampersandOffset)));
            }
            pos = ampersandOffset + 1;
        }
        return result;
    }

    private static interface StringSubstitutor {
        String replaceOneString(String body, Request request);
        boolean matchesFound(String body);
    }

    private static class ParameterSubstitutor implements StringSubstitutor {
        @Override
        public String replaceOneString(String body, Request request) {
            Matcher parameterMatcher = PARAMETER.matcher(body);
            parameterMatcher.find();
            String match = parameterMatcher.group();
            String parameter = "";
            Map<String, String> query = getQueryFromUri(match);
            try {
                if (query.containsKey(INDEX_PARAMETER)) {
                    int index = Integer.parseInt(query.get(INDEX_PARAMETER));
                    if (index < request.url().querySize()) {
                        parameter = request.url().queryParameterValue(index);
                    }
                } else if (query.containsKey(NAME_PARAMETER)) {
                    String name = query.get(NAME_PARAMETER);
                    if (request.url().queryParameterNames().contains(name)) {
                        parameter = request.url().queryParameterValues(name).get(0);
                    }
                }
                body = parameterMatcher.replaceFirst(parameter);
            } catch (Exception e) {
                Timber.e(e, "You did something wrong when setting up your parameter interceptor. ");
            }
            return body;
        }

        @Override
        public boolean matchesFound(String body) {
            Matcher parameterMatcher = PARAMETER.matcher(body);
            return parameterMatcher.find();
        }
    }

    private static class DateSubstitutor implements StringSubstitutor {
        @Override
        public String replaceOneString(String body, Request request) {
            Matcher dateMatcher = DATE.matcher(body);
            dateMatcher.find();
            String match = dateMatcher.group();
            Map<String, String> query = getQueryFromUri(match);
            LocalDate date = new LocalDate();
            if(query.containsKey(OFFSET_PARAMETER)) {
                date = date.plusDays(Integer.parseInt(query.get(OFFSET_PARAMETER)));
            }
            body = dateMatcher.replaceFirst(date.toString());
            return body;
        }

        @Override
        public boolean matchesFound(String body) {
            Matcher dateMatcher = DATE.matcher(body);
            return dateMatcher.find();
        }
    }

    private static class DateTimeSubstitutor implements StringSubstitutor {
        @Override
        public String replaceOneString(String body, Request request) {
            Matcher dateTimeMatcher = DATETIME.matcher(body);
            dateTimeMatcher.find();

            String match = dateTimeMatcher.group();
            Map<String, String> query = getQueryFromUri(match);
            DateTime time = new DateTime();
            if(query.containsKey(OFFSET_PARAMETER)) {
                time = time.plusDays(Integer.parseInt(query.get(OFFSET_PARAMETER)));
            }
            String timeString = time.toString();
            body = dateTimeMatcher.replaceFirst(timeString);
            return body;
        }

        @Override
        public boolean matchesFound(String body) {
            Matcher dateTimeMatcher = DATETIME.matcher(body);
            return dateTimeMatcher.find();
        }
    }

    private void addSubstitutors() {
        mSubstitutorList = new ArrayList<>();
        mSubstitutorList.add(new DateTimeSubstitutor());
        mSubstitutorList.add(new DateSubstitutor());
        mSubstitutorList.add(new ParameterSubstitutor());
    }

}
