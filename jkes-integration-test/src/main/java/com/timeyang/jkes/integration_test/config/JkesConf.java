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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
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
        return replaceDomainNameWithIp(this.kafkaBootstrapServers);
    }

    @Override
    public String getKafkaConnectServers() {
        return kafkaConnectServers;
    }

    @Override
    public String getEsBootstrapServers() {
        return replaceDomainNameWithIp(this.esBootstrapServers);
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

    // mainly used for test. in inner env, use ip directly or domainName if dns parse is available
    private String replaceDomainNameWithIp(String u) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] urls = u.split(",");
        Arrays.stream(urls).forEach(urlStr -> {
            if(urlStr.startsWith("http")) {
                try {
                    URL url = new URL(urlStr);
                    InetAddress address = InetAddress.getByName(url.getHost());
                    String ip = address.getHostAddress();
                    stringBuilder
                            .append(url.getProtocol()).append("://")
                            .append(ip).append(":")
                            .append(url.getPort())
                            .append(",");
                } catch (UnknownHostException | MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }else {
                String[] split = urlStr.split(":");
                try {
                    stringBuilder.append(HttpUtils.getIpsFromDomainName(split[0])).append(":").append(split[1]).append(",");
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }
}
