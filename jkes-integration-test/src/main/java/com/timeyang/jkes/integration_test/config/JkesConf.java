package com.timeyang.jkes.integration_test.config;

import com.timeyang.jkes.core.http.HttpUtils;
import com.timeyang.jkes.core.support.Config;
import com.timeyang.jkes.core.support.JkesProperties;
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
public class JkesConf implements JkesProperties {

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

    @Value("${kafka.topic.prefix}")
    private String kafkaTopicPrefix;

    @Value("${kafka.connector.prefix}")
    private String kafkaConnectorPrefix;

    @Value("${kafka.connect.servers}")
    private String kafkaConnectServers;

    @Value("${es.bootstrap.servers}")
    private String esBootstrapServers;

    @Value("${document.base_package}")
    private String documentBasePackage;

    @Value("${es.index.prefix}")
    private String esIndexPrefix;

    @Value("${es.alias.prefix}")
    private String esAliasPrefix;

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
    public String getKafkaTopicPrefix() {
        return kafkaTopicPrefix;
    }

    @Override
    public String getKafkaConnectorPrefix() {
        return kafkaConnectorPrefix;
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
    public String getEsIndexPrefix() {
        return esIndexPrefix;
    }

    @Override
    public String getEsAliasPrefix() {
        return esAliasPrefix;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public void setKafkaTopicPrefix(String kafkaTopicPrefix) {
        this.kafkaTopicPrefix = kafkaTopicPrefix;
    }

    public void setKafkaConnectorPrefix(String kafkaConnectorPrefix) {
        this.kafkaConnectorPrefix = kafkaConnectorPrefix;
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

    public void setEsIndexPrefix(String esIndexPrefix) {
        this.esIndexPrefix = esIndexPrefix;
    }

    public void setEsAliasPrefix(String esAliasPrefix) {
        this.esAliasPrefix = esAliasPrefix;
    }


}
