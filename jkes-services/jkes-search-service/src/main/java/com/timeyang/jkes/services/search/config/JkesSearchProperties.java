package com.timeyang.jkes.services.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chaokunyang
 */
@Data
@ConfigurationProperties(prefix = "jkes")
public class JkesSearchProperties {
    private ESConfig es;

    @Data
    public static class ESConfig {
        private String servers;
    }
}
