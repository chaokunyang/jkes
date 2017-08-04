package com.timeyang.jkes.services.search.api.v1;

import com.timeyang.jkes.services.search.domain.GetResponseProjection;
import com.timeyang.jkes.services.search.domain.SearchResponseProjection;
import com.timeyang.jkes.services.search.elasticsearch.EsRestClient;
import com.timeyang.jkes.services.search.exception.SearchException;
import com.timeyang.jkes.services.search.util.JsonUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author chaokunyang
 */
@Service
public class SearchService {

    private final EsRestClient esRestClient;

    @Autowired
    public SearchService(EsRestClient esRestClient) {
        this.esRestClient = esRestClient;
    }

    public GetResponseProjection get(String index, String type, String id) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(index)
                .append("/").append(type)
                .append("/").append(id);
        Response response = esRestClient.performRequest("GET", sb.toString());

        try {
            String s = EntityUtils.toString(response.getEntity());
            GetResponseProjection responseProjection = JsonUtils.parseJsonToObject(s, GetResponseProjection.class);

            return responseProjection;
        } catch (IOException e) {
            throw new SearchException(e);
        }
    }

    public SearchResponseProjection search(String index, String type, JSONObject request, String query) {
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
            SearchResponseProjection responseProjection = JsonUtils.parseJsonToObject(s, SearchResponseProjection.class);
            return responseProjection;

        } catch (IOException e) {
            throw new SearchException(e);
        }

    }
}
