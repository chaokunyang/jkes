package com.timeyang.jkes.core.kafka.connect;

import com.timeyang.jkes.core.kafka.util.EsKafkaConnectUtils;
import com.timeyang.jkes.core.support.Config;
import com.timeyang.jkes.core.support.DefaultJkesPropertiesImpl;
import com.timeyang.jkes.core.support.JkesProperties;
import com.timeyang.jkes.entity.Person;
import org.junit.Test;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class KafkaConnectClientTest {

    @Test
    public void createEsSinkConnector() throws Exception {
        JkesProperties jkesProperties = new DefaultJkesPropertiesImpl() {
            @Override
            public String getKafkaConnectServers() {
                return "http://k1-test.com:8083,http://k2-test.com:8083,http://k3-test.com:8083";
            }
        };
        Config.setJkesProperties(jkesProperties);

        KafkaConnectClient kafkaConnectClient = new KafkaConnectClient(jkesProperties);
        String connectorName = EsKafkaConnectUtils.getConnectorName(Person.class);
        if(!kafkaConnectClient.checkConnectorExists(connectorName))
            kafkaConnectClient.createEsSinkConnector(Person.class);
    }

    @Test
    public void deleteConnector() throws Exception {
        JkesProperties jkesProperties = new DefaultJkesPropertiesImpl() {
            @Override
            public String getKafkaConnectServers() {
                return "http://k1-test.com:8083,http://k2-test.com:8083,http://k3-test.com:8083";
            }
        };
        Config.setJkesProperties(jkesProperties);

        String connectorName = EsKafkaConnectUtils.getConnectorName(Person.class);
        new KafkaConnectClient(jkesProperties).deleteConnector(connectorName);
    }

    @Test
    public void checkConnectorExists() throws Exception {
        JkesProperties jkesProperties = new DefaultJkesPropertiesImpl() {
            @Override
            public String getKafkaConnectServers() {
                return "http://k1-test.com:8083,http://k2-test.com:8083,http://k3-test.com:8083";
            }
        };
        Config.setJkesProperties(jkesProperties);

        String connectorName = EsKafkaConnectUtils.getConnectorName(Person.class);
        System.out.println(new KafkaConnectClient(jkesProperties).checkConnectorExists(connectorName));
    }

}