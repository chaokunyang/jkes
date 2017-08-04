package com.timeyang.jkes.core.kafka.connect;

import com.timeyang.jkes.core.support.Config;
import org.json.JSONObject;

/**
 * Connector config template factory.
 * @author chaokunyang
 */
class ConnectorConfigTemplates {

    static JSONObject getEsSinkConfigTemplate() {
        JSONObject config = new JSONObject();
        config.put("connector.class", "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector");
        config.put("tasks.max", "10");
        String connectServers = Config.getJkesProperties().getEsBootstrapServers();
        config.put("connection.url", connectServers);
        config.put("batch.size", "2000");
        config.put("linger.ms", "5");
        config.put("max.in.flight.requests", "11");
        config.put("key.ignore", "false");
        config.put("schema.ignore", "true");
        config.put("retry.backoff.ms", "100");

        return config;
    }

}
