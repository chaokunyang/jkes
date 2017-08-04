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

    String getKafkaTopicPrefix();

    String getKafkaConnectServers();

    String getKafkaConnectorPrefix();

    String getEsBootstrapServers();

    String getDocumentBasePackage();

    String getEsIndexPrefix();

    String getEsAliasPrefix();

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

        if(StringUtils.containsWhitespace(jkesProperties.getKafkaTopicPrefix())) {
            throw new IllegalConfigException("kafkaTopicPrefix can't contain white space");
        }
        if(!StringUtils.hasText(jkesProperties.getKafkaTopicPrefix())) {
            throw new IllegalConfigException("kafkaTopicPrefix must be configured");
        }

        if(!StringUtils.hasText(jkesProperties.getKafkaConnectServers())) {
            throw new IllegalConfigException("kafkaConnectServers must be configured");
        }

        if(StringUtils.containsWhitespace(jkesProperties.getKafkaConnectorPrefix())) {
            throw new IllegalConfigException("kafkaConnectorPrefix can't contain white space");
        }
        if(!StringUtils.hasText(jkesProperties.getKafkaConnectorPrefix())) {
            throw new IllegalConfigException("kafkaConnectorPrefix must be configured");
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

        if(StringUtils.containsWhitespace(jkesProperties.getEsIndexPrefix())) {
            throw new IllegalConfigException("esIndexPrefix can't contain white space");
        }
        if(!StringUtils.hasText(jkesProperties.getEsIndexPrefix())) {
            throw new IllegalConfigException("esIndexPrefix must be configured");
        }

        if(StringUtils.containsWhitespace(jkesProperties.getEsAliasPrefix())) {
            throw new IllegalConfigException("esAliasPrefix can't contain white space");
        }
        if(!StringUtils.hasText(jkesProperties.getEsAliasPrefix())) {
            throw new IllegalConfigException("esAliasPrefix must be configured");
        }

        return true;
    }

}
