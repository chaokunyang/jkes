package com.timeyang.jkes.core.kafka.connect;

import com.timeyang.jkes.DocumentMetadata;
import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.http.HttpClient;
import com.timeyang.jkes.core.http.Response;
import com.timeyang.jkes.core.kafka.exception.KafkaConnectRequestException;
import com.timeyang.jkes.core.kafka.util.KafkaConnectUtils;
import com.timeyang.jkes.core.kafka.util.KafkaUtils;
import com.timeyang.jkes.core.support.JkesProperties;
import com.timeyang.jkes.core.util.Asserts;
import com.timeyang.jkes.core.util.DocumentUtils;
import com.timeyang.jkes.core.util.JsonUtils;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Kafka Connect Rest Client
 * <p>This class is <strong>ThreadSafe</strong></p>
 *
 * @author chaokunyang
 */
@Named
public class KafkaConnectClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.getInstance();

    private final DocumentMetadata documentMetadata;
    private final String[] urls;

    // locks for connectors
    private final ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<>();

    @Inject
    public KafkaConnectClient(JkesProperties jkesProperties, DocumentMetadata documentMetadata) {
        this.urls = jkesProperties.getKafkaConnectServers().split("\\s*,");
        this.documentMetadata = documentMetadata;
    }

    @PostConstruct
    public void init() {
        documentMetadata.getAnnotatedClasses().forEach(clazz -> {
            String connectorName = KafkaConnectUtils.getConnectorName(clazz);
            if(checkConnectorExists(connectorName)) {
                updateEsSinkConnector(clazz);
            }else {
                createEsSinkConnector(clazz);
            }
        });
    }

    public JSONObject getConnectConfig(String connectorName) {
        try {
            Response response = HTTP_CLIENT.get(getRandomUrl(), String.format("connectors/%s/config", connectorName));
            if(response.getStatusCode() / 100 != 2) return null;

            return JsonUtils.parseJsonToObject(response.getContent(), JSONObject.class);
        } catch (IOException e) {
            throw new KafkaConnectRequestException("Failed get connector[" + connectorName + "] config", e);
        }
    }

    /**
     * <p>create es sink connector for document to be indexed</p>
     * <strong>Note if topic doesn't exist, kafka connect will create it automatically. So there is no need to wait topic created to create connector</strong>
     *
     * @param document document to be indexed
     */
    public void createEsSinkConnector(Class<?> document) {
        Asserts.check(document.isAnnotationPresent(Document.class), document + " must be annotated with " + Document.class + " to create kafka connector");

        String connectorName = KafkaConnectUtils.getConnectorName(document);

        if(checkConnectorExists(connectorName))
            throw new IllegalArgumentException("The specified class " + document + " already have a corresponding connector[" + connectorName + "]");

        JSONObject payload = new JSONObject();
        payload.put("name", connectorName);

        JSONObject config = ConnectorConfigTemplates.getEsSinkConfigTemplate();
        String topic = KafkaUtils.getTopic(document);
        config.put("topics", topic);
        config.put("type.name", DocumentUtils.getTypeName(document));
        config.put("topic.index.map", getTopicIndexMap(topic, document));

        payload.put("config", config);
        try {
            String url = getRandomUrl();

            // happen-before is ensured by ConcurrentMap
            // see http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/package-summary.html Memory Consistency Properties
            locks.putIfAbsent(connectorName, new ReentrantLock());
            try {
                locks.get(connectorName).lock();
                HTTP_CLIENT.post(url, "connectors", payload);
            }finally {
                locks.get(connectorName).unlock();
            }

        } catch (IOException e) {
            throw new KafkaConnectRequestException(e);
        }
    }

    public boolean createEsSinkConnectorIfAbsent(Class<?> document) {
        Asserts.check(document.isAnnotationPresent(Document.class), document + " must be annotated with " + Document.class + " to create kafka connector");
        String connectorName = KafkaConnectUtils.getConnectorName(document);

        locks.putIfAbsent(connectorName, new ReentrantLock());
        try {
            locks.get(connectorName).lock();
            if(!checkConnectorExists(connectorName)) {
                createEsSinkConnector(document);
                return true;
            }
        }finally {
            locks.get(connectorName).unlock();
        }

        return false;
    }

    public void updateEsSinkConnector(Class<?> document) {
        Asserts.check(document.isAnnotationPresent(Document.class), document + " must be annotated with " + Document.class + " to update kafka connector");

        String connectorName = KafkaConnectUtils.getConnectorName(document);

        JSONObject config = ConnectorConfigTemplates.getEsSinkConfigTemplate();
        String topic = KafkaUtils.getTopic(document);
        config.put("topics", topic);
        config.put("type.name", DocumentUtils.getTypeName(document));
        config.put("topic.index.map", getTopicIndexMap(topic, document));

        try {
            locks.putIfAbsent(connectorName, new ReentrantLock());
            try {
                locks.get(connectorName).lock();
                HTTP_CLIENT.put(getRandomUrl(), String.format("connectors/%s/config", connectorName), config);
            }finally {
                locks.get(connectorName).unlock();
            }

        } catch (IOException e) {
            throw new KafkaConnectRequestException(e);
        }
    }

    /**
     * ensure elasticsearch sink connector is running.
     * <p>If elasticsearch sink connector doesn't exist, then create it.
     * If elasticsearch sink connector failed some how, then restart it</p>
     * @param document document
     * @return {@code true} if elasticsearch sink connector is up
     */
    public boolean ensureEsSinkConnector(Class<?> document) {
        throw new UnsupportedOperationException();
    }

    /**
     * delete specified connector
     * @param connectorName connector name
     */
    public void deleteConnector(String connectorName) {
        if(!checkConnectorExists(connectorName))
            throw new IllegalArgumentException("The specified connector[" + connectorName + "] doesn't exist");
        String endpoint = "connectors/" + connectorName;
        try {
            String url = getRandomUrl();

            locks.putIfAbsent(connectorName, new ReentrantLock());
            try {
                locks.get(connectorName).lock();
                HTTP_CLIENT.delete(url, endpoint);
            }finally {
                locks.get(connectorName).unlock();
            }
        } catch (IOException e) {
            throw new KafkaConnectRequestException(e);
        }
    }

    /**
     * restart specified connector
     * @param connectorName connector name
     */
    public void restartConnector(String connectorName) {
        String endpoint = "connectors/" + connectorName + "/restart";
        try {
            String url = getRandomUrl();

            locks.putIfAbsent(connectorName, new ReentrantLock());
            try {
                locks.get(connectorName).lock();
                HTTP_CLIENT.post(url, endpoint, null);
            }finally {
                locks.get(connectorName).unlock();
            }
        } catch (IOException e) {
            throw new KafkaConnectRequestException(e);
        }
    }

    /**
     * Check whether connector exists
     * @param connectorName connector name
     * @return whether connector exists
     */
    public boolean checkConnectorExists(String connectorName) {
        String endpoint = "connectors/" + connectorName;
        try {
            String url = getRandomUrl();

            Response response;
            locks.putIfAbsent(connectorName, new ReentrantLock());
            try {
                locks.get(connectorName).lock();
                response = HTTP_CLIENT.get(url, endpoint);
            }finally {
                locks.get(connectorName).unlock();
            }

            boolean contain = (response.getStatusCode() / 100) == 2;
            return contain;
        } catch (IOException e) {
            throw new KafkaConnectRequestException(e);
        }
    }

    /**
     * For the topic.index.map config, you need something like topic.index.map=topic1:index1, topic2:index2.
     * This config is intended to set the topic to index map for multiple topics.
     * If you want to set mapping to multiple topics, you can use topic.index.map=topic1:index1, topic2:index2.
     * <i>https://github.com/confluentinc/kafka-connect-elasticsearch/issues/12</i>
     * @param topic topic name
     * @param document document will be indexed
     * @return topicIndexMap
     */
    private String getTopicIndexMap(String topic, Class<?> document) {
        String index = DocumentUtils.getIndexName(document);
        String topicIndexMap = topic + ":" + index;

        return topicIndexMap;
    }

    private String getRandomUrl() {
        Random random = new Random();
        int index = random.nextInt(this.urls.length);
        return this.urls[index];
    }
}
