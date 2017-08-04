package com.timeyang.jkes.services.search.elasticsearch;

import com.timeyang.jkes.services.search.config.JkesSearchProperties;
import com.timeyang.jkes.services.search.exception.SearchException;
import com.timeyang.jkes.services.search.exception.SearchResponseException;
import com.timeyang.jkes.services.search.util.JsonUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * The RestClient class is thread-safe and ideally has the same lifecycle as the application that uses it.
 * @author chaokunyang
 */
@Component
public class EsRestClient {

    private RestClient restClient;

    private Sniffer sniffer;

    @Autowired
    public EsRestClient(JkesSearchProperties jkesProperties) {
        SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
        Header[] defaultHeaders = {new BasicHeader("Content-Type", "application/json")};

        String[] urls = jkesProperties.getEs().getServers().split("\\s*,");
        HttpHost[] hosts = new HttpHost[urls.length];
        for (int i = 0; i < urls.length; i++) {
            hosts[i] = HttpHost.create(urls[i]);
        }

        RestClient restClient = RestClient.builder(hosts)
                .setRequestConfigCallback(requestConfigBuilder -> {
                    return requestConfigBuilder.setConnectTimeout(5000) // default 1s
                            .setSocketTimeout(60000); // defaults to 30 seconds
                }).setHttpClientConfigCallback(httpClientBuilder -> {
                    return httpClientBuilder.setDefaultIOReactorConfig(
                            IOReactorConfig.custom().setIoThreadCount(2).build()); // because only used for admin, so not necessary to hold many worker threads
                })
                .setMaxRetryTimeoutMillis(60000) // defaults to 30 seconds
                .setDefaultHeaders(defaultHeaders)
                .setFailureListener(sniffOnFailureListener)
                .build();

        Sniffer sniffer = Sniffer.builder(restClient).build();
        sniffOnFailureListener.setSniffer(sniffer);

        this.sniffer = sniffer;
        this.restClient = restClient;
    }

    /**
     * It is important that restClient gets closed when no longer needed so that all the resources used by it get properly released, as well as the underlying http client instance and its threads
     */
    @PreDestroy
    public void close() {
        try {
            sniffer.close();
            restClient.close();
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch Rest Client close exception", e);
        }
    }

    public Response performRequest(String method, String endpoint) {
        try {
            return this.restClient.performRequest(method, endpoint);
        } catch (ResponseException e) {
            throw new SearchResponseException(e);
        } catch (IOException e) {
            throw new SearchException(e);
        }
    }

    public Response performRequest(String method, String endpoint, JSONObject entity) {
        return performRequest(method, endpoint, Collections.<String, String>emptyMap(), entity);
    }

    public Response performRequest(String method, String endpoint, Map<String, String> params,
                                   JSONObject entity) {
        return performRequest(method, endpoint, params, entity, new Header[]{});
    }

    public Response performRequest(String method, String endpoint, Map<String, String> params,
                                   JSONObject entity, Header... headers) {
        try {
            HttpEntity payload = new NStringEntity(JsonUtils.convertToString(entity), ContentType.APPLICATION_JSON);
            return restClient.performRequest(method, endpoint, params, payload, HttpAsyncResponseConsumerFactory.DEFAULT, headers);
        } catch (ResponseException e) {
            throw new SearchResponseException(e);
        } catch (IOException e) {
            throw new SearchException(e);
        }
    }


}
