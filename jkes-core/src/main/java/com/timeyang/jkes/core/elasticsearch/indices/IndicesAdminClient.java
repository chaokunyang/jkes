package com.timeyang.jkes.core.elasticsearch.indices;

import com.timeyang.jkes.core.Metadata;
import com.timeyang.jkes.core.elasticsearch.EsRestClient;
import com.timeyang.jkes.core.support.JkesProperties;
import com.timeyang.jkes.core.util.DocumentUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Response;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.timeyang.jkes.core.elasticsearch.AnnotationConfigChecker.check;

/**
 * Indices Admin Client
 *
 * @author chaokunyang
 */
@Named
public class IndicesAdminClient {

    private static final Log logger = LogFactory.getLog(IndicesAdminClient.class);

    // locks for indices, prevent concurrently change an index
    private ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<>();

    private final JkesProperties jkesProperties;
    private final Metadata metadata;
    private final EsRestClient esRestClient;
    private IndicesBuilder indicesBuilder = IndicesBuilder.getInstance();

    @Inject
    public IndicesAdminClient(EsRestClient esRestClient, JkesProperties jkesProperties, Metadata metadata) {
        this.esRestClient = esRestClient;
        this.jkesProperties = jkesProperties;
        this.metadata = metadata;
    }

    @PostConstruct
    public void init() {
        logger.info("search init begin");

        Set<Class<?>> annotatedClasses = metadata.getAnnotatedDocuments();

        check(annotatedClasses);

        for(Class clazz : annotatedClasses) {
            if(checkExists(DocumentUtils.getIndexName(clazz))) {
                updateIndex(clazz);
            }else {
                createIndex(clazz);
            }
        }

        logger.info("search init finished");
    }

    public void createIndex(Class clazz) {
        String indexName = DocumentUtils.getIndexName(clazz);
        JSONObject index = this.indicesBuilder.buildIndex(clazz);

        locks.putIfAbsent(indexName, new ReentrantLock());
        locks.get(indexName).lock();
        this.esRestClient.performRequest("PUT", indexName, index);
        locks.get(indexName).unlock();

        logger.info("created index[" + indexName + "] for entity: " + clazz);
    }

    public void updateIndex(Class clazz) {
        String indexName = DocumentUtils.getIndexName(clazz);
        logger.info("update index[" + indexName + "] for entity: " + clazz);
        JSONObject index = this.indicesBuilder.buildIndex(clazz);

        // update index mappings
        String type = DocumentUtils.getTypeName(clazz);
        String mappingEndpoint = indexName + "/_mapping/" + type;
        JSONObject mapping = index.getJSONObject("mappings").getJSONObject(type);

        locks.putIfAbsent(indexName, new ReentrantLock());
        locks.get(indexName).lock();
        this.esRestClient.performRequest("PUT", mappingEndpoint, mapping);
        locks.get(indexName).unlock();

        // TODO update aliases

    }

    public void deleteIndex(String index) {
        locks.putIfAbsent(index, new ReentrantLock());
        locks.get(index).lock();
        this.esRestClient.performRequest("DELETE", index);
        locks.get(index).unlock();

        logger.info("deleted index[" + index + "]");
    }

    public boolean checkExists(String index) {
        locks.putIfAbsent(index, new ReentrantLock());
        locks.get(index).lock();
        Response response = this.esRestClient.performRequest("HEAD", index);
        locks.get(index).unlock();

        return response.getStatusLine().getStatusCode() == 200;
    }

}
