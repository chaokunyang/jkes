package com.timeyang.jkes.services.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.json.JSONObject;

/**
 * @author chaokunyang
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetResponseProjection {
    private JSONObject _source;
}
