package com.timeyang.jkes.core.support;

import com.timeyang.jkes.core.exception.IllegalConfigException;
import com.timeyang.jkes.core.util.StringUtils;

/**
 * Search Framework Config
 * <p><strong>Need to used with {@link Config#setJkesProperties(JkesProperties)}</strong></p>
 *
 * @author chaokunyang
 */
public interface JkesProperties {

    String getKafkaBootstrapServers();

    String getKafkaConnectServers();

    String getEsBootstrapServers();

    String getDocumentBasePackage();

    String getClientId();

    /**
     * if config is legal, return true, else throw IllegalConfigException
     * @param jkesProperties JkesProperties config
     * @return legal
     * @throws IllegalConfigException
     */
    static boolean check(JkesProperties jkesProperties) {
        if(!StringUtils.hasText(jkesProperties.getKafkaBootstrapServers())) {
            throw new IllegalConfigException("kafkaBootstrapServers must be configured");
        }

        if(StringUtils.containsWhitespace(jkesProperties.getClientId())) {
            throw new IllegalConfigException("clientId can't contain white space");
        }
        if(!StringUtils.hasText(jkesProperties.getClientId())) {
            throw new IllegalConfigException("clientId must be configured");
        }

        if(!StringUtils.hasText(jkesProperties.getKafkaConnectServers())) {
            throw new IllegalConfigException("kafkaConnectServers must be configured");
        }

        if(!StringUtils.hasText(jkesProperties.getEsBootstrapServers())) {
            throw new IllegalConfigException("esBootstrapServers must be configured");
        }

        if(StringUtils.containsWhitespace(jkesProperties.getDocumentBasePackage())) {
            throw new IllegalConfigException("documentBasePackage can't contain white space");
        }
        if(!StringUtils.hasText(jkesProperties.getDocumentBasePackage())) {
            throw new IllegalConfigException("documentBasePackage must be configured");
        }

        return true;
    }

}
