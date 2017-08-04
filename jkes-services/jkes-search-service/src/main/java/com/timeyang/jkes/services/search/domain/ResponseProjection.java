package com.timeyang.jkes.services.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Response projection
 *
 * @author chaokunyang
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResponseProjection {
    private float took;
    private Hits hits;
}
