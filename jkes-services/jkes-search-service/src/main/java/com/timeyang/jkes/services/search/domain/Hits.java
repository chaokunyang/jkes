package com.timeyang.jkes.services.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.json.JSONObject;

/**
 * @author chaokunyang
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Hits {
    private long total;
    private Item[] hits;
    private float max_score;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Item {
        private String _id;
        private float _score;
        private JSONObject _source;
    }
}