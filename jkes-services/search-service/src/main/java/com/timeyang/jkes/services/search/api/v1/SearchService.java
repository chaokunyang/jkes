package com.timeyang.jkes.services.search.api.v1;

import com.timeyang.jkes.services.search.domain.ResponseProjection;
import com.timeyang.jkes.services.search.elasticsearch.EsJavaClient;
import com.timeyang.jkes.services.search.elasticsearch.EsRestClient;
import com.timeyang.jkes.services.search.util.JsonUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * @author chaokunyang
 */
@Service
public class SearchService {

    private final EsRestClient esRestClient;
    private final EsJavaClient esJavaClient;

    @Autowired
    public SearchService(EsJavaClient esJavaClient, EsRestClient esRestClient) {
        this.esJavaClient = esJavaClient;
        this.esRestClient = esRestClient;

    }

    public Map<String, Object> get(String index, String type, String id) {
        return esJavaClient.get(index, type, id);
    }

    public ResponseProjection search(String index, String type, JSONObject request, String query) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("/")
                .append(index)
                .append("/").append(type)
                .append("/_search");
        if(query!=null)
            sb.append("?").append(query);
        if(request == null)
            request = new JSONObject();
        Response response = esRestClient.performRequest("POST", sb.toString(), request);

        try {
            String s = EntityUtils.toString(response.getEntity());
            ResponseProjection responseProjection = JsonUtils.parseJsonToObject(s, ResponseProjection.class);
            return responseProjection;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
