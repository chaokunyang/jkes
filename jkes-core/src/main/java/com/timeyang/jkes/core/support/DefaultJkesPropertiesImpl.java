package com.timeyang.jkes.core.support;

/**
 * DefaultJkesPropertiesImpl, mostly used for test
 *
 * @author chaokunyang
 */
public class DefaultJkesPropertiesImpl implements JkesProperties {
    @Override
    public String getKafkaBootstrapServers() {
        return null;
    }

    @Override
    public String getKafkaConnectServers() {
        return null;
    }

    @Override
    public String getEsBootstrapServers() {
        return null;
    }

    @Override
    public String getDocumentBasePackage() {
        return null;
    }

    @Override
    public String getKafkaTopicPrefix() {
        return null;
    }

    @Override
    public String getKafkaConnectorPrefix() {
        return null;
    }

    @Override
    public String getEsIndexPrefix() {
        return null;
    }

    @Override
    public String getEsAliasPrefix() {
        return null;
    }
}
