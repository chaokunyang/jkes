package com.timeyang.jkes.integration_test.config;

import com.timeyang.jkes.core.http.HttpUtils;
import com.timeyang.jkes.core.support.Config;
import com.timeyang.jkes.core.support.DefaultJkesPropertiesImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author chaokunyang
 */
@Configuration
@PropertySource("classpath:jkes.properties")
public class JkesConf extends DefaultJkesPropertiesImpl {

    @Bean
    public static PropertySourcesPlaceholderConfigurer
    propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @PostConstruct
    public void setUp() {
        Config.setJkesProperties(this);
    }


    @Value("${kafka.bootstrap.servers}")
    private String kafkaBootstrapServers;

    @Value("${kafka.connect.servers}")
    private String kafkaConnectServers;

    @Value("${es.bootstrap.servers}")
    private String esBootstrapServers;

    @Value("${document.base_package}")
    private String documentBasePackage;

    @Value("${jkes.client.id}")
    private String clientId;

    @Override
    public String getKafkaBootstrapServers() {
        StringBuilder stringBuilder = new StringBuilder();
        String[] urls = kafkaBootstrapServers.split(",");
        Arrays.stream(urls).forEach(url -> {
            String[] split = url.split(":");
            try {
                stringBuilder.append(HttpUtils.getIpsFormDomainName(split[0])).append(":").append(split[1]).append(",");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });

        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }

    @Override
    public String getKafkaConnectServers() {
        return kafkaConnectServers;
    }

    @Override
    public String getEsBootstrapServers() {
        return esBootstrapServers;
    }

    @Override
    public String getDocumentBasePackage() {
        return documentBasePackage;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public void setKafkaConnectServers(String kafkaConnectServers) {
        this.kafkaConnectServers = kafkaConnectServers;
    }

    public void setEsBootstrapServers(String esBootstrapServers) {
        this.esBootstrapServers = esBootstrapServers;
    }

    public void setDocumentBasePackage(String documentBasePackage) {
        this.documentBasePackage = documentBasePackage;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
