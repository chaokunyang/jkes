package com.timeyang.jkes.core.http;

import com.timeyang.jkes.core.util.Asserts;
import com.timeyang.jkes.core.util.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * HttpClient backed by Apache HttpComponents Client
 * <p>{@link HttpClient} is thread-safe</p>
 * @author chaokunyang
 */
public class HttpClient {

    private static final Logger logger = Logger.getLogger(HttpClient.class);

    private static final HttpClient CLIENT = new HttpClient();

    private CloseableHttpClient httpclient = HttpClients.createDefault();

    private HttpClient() {}

    public Response get(String host, String endpoint) throws IOException {
        return performRequest("GET", host, endpoint, Collections.emptyMap(), null);
    }

    public Response post(String host, String endpoint, JSONObject entity) throws IOException {
        return performRequest("POST", host, endpoint, Collections.emptyMap(), entity);
    }

    public Response put(String host, String endpoint, JSONObject entity) throws IOException {
        return performRequest("PUT", host, endpoint, Collections.emptyMap(), entity);
    }

    public Response delete(String host, String endpoint) throws IOException {
        return performRequest("DELETE", host, endpoint, Collections.emptyMap(), null);
    }

    public Response head(String host, String endpoint) throws IOException {
        return performRequest("HEAD", host, endpoint, Collections.emptyMap(), null);
    }

    public Response performRequest(String method, String host, String endpoint, Map<String, String> params, JSONObject entity) throws IOException {
        HttpRequestBase httpRequestBase = buildRequest(method, host, endpoint, params, entity);

        try (CloseableHttpResponse response = httpclient.execute(httpRequestBase)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity responseEntity = response.getEntity();

            Response resp = Response.builder()
                    .statusCode(statusLine.getStatusCode())
                    .content(responseEntity == null ? null : EntityUtils.toString(responseEntity))
                    .build();

            EntityUtils.consume(responseEntity);

            return resp;
        }
    }

    private HttpRequestBase buildRequest(String method, String host, String endpoint, Map<String, String> params, JSONObject entity) {
        HttpRequestBase httpRequestBase;
        URI uri = buildUri(host + "/" + endpoint, params);
        if(method.equalsIgnoreCase("GET")) {
            Asserts.check(entity == null, "Http GET request with request body is not allowed currently");
            httpRequestBase = new HttpGet(uri);
        }else if(method.equalsIgnoreCase("POST")) {
            HttpPost httpPost = new HttpPost(uri);
            HttpEntity payload = new ByteArrayEntity(JsonUtils.convertToBytes(entity), ContentType.APPLICATION_JSON);
            httpPost.setEntity(payload);
            httpRequestBase = httpPost;
        }else if(method.equalsIgnoreCase("PUT")) {
            HttpPut httpPut = new HttpPut(uri);
            HttpEntity payload = new ByteArrayEntity(JsonUtils.convertToBytes(entity), ContentType.APPLICATION_JSON);
            httpPut.setEntity(payload);
            httpRequestBase = httpPut;
        }else if(method.equalsIgnoreCase("DELETE")) {
            Asserts.check(entity == null, "Http DELETE request with request body is not allowed currently");
            httpRequestBase = new HttpDelete(uri);
        }else if(method.equalsIgnoreCase("HEAD")) {
            Asserts.check(entity == null, "Http HEAD request with request body is not allowed");
            httpRequestBase = new HttpHead(uri);
        }else {
            throw new UnsupportedOperationException("http method[" + method + "] is not supported currently");
        }

        return httpRequestBase;
    }

    static URI buildUri(String path, Map<String, String> params) {
        Objects.requireNonNull(path, "path must not be null");
        try {
            URIBuilder uriBuilder = new URIBuilder(path);
            for (Map.Entry<String, String> param : params.entrySet()) {
                uriBuilder.addParameter(param.getKey(), param.getValue());
            }
            return uriBuilder.build();
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static HttpClient getInstance() {
        return CLIENT;
    }
}
