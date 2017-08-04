package com.timeyang.jkes.services.search.elasticsearch;

import com.timeyang.jkes.services.search.config.JkesSearchProperties;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author chaokunyang
 */
@Component
public class EsJavaClient {

    private final TransportClient client;

    public EsJavaClient(JkesSearchProperties jkesProperties) {
        Settings settings = Settings.builder()
                .put("cluster.name", "ucloud-search")
                .put("client.transport.sniff", true).build();

        String[] urls = jkesProperties.getEs_servers().split("\\s*,");
        TransportAddress[] addresses = new TransportAddress[urls.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                addresses[i] = new InetSocketTransportAddress(InetAddress.getByName(urls[i]), 9300);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        this.client = new PreBuiltTransportClient(settings)
                .addTransportAddresses(addresses);
    }

    @PreDestroy
    public void close() {
        this.client.close();
    }

    public Map<String, Object> get(String index, String type, String id) {
        GetResponse response = this.client.prepareGet(index, type, id).get();
        return response.getSource();
    }

}
