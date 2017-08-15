package com.timeyang.jkes.core.kafka.producer;

import com.timeyang.jkes.core.exception.JkesException;
import com.timeyang.jkes.core.kafka.serialize.json.JkesKafkaJsonSerializer;
import com.timeyang.jkes.core.metadata.DocumentMetadata;
import com.timeyang.jkes.core.metadata.Metadata;
import com.timeyang.jkes.core.support.JkesProperties;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * kafka producer
 *
 * @author chaokunyang
 */
@Named
public final class JkesKafkaProducer {

    private KafkaProducer<String, Object> producer;

    @Inject
    public JkesKafkaProducer(JkesProperties jkesProperties) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, jkesProperties.getKafkaBootstrapServers());
        props.put("acks", "all");
        props.put("retries", 1);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("documentBasePackage", jkesProperties.getDocumentBasePackage());

        // Why use StringSerializer? Because in some cases key is String not Long, such as MongoDB
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JkesKafkaJsonSerializer.class);

        this.producer = new KafkaProducer<>(props);
    }

    public void send(Object value) {
        // String topic = KafkaUtils.getTopic(value);
        // String key = KafkaUtils.getKey(value);
        DocumentMetadata documentMetadata =
                Metadata.getMetadata().getMetadataMap().get(value.getClass());

        String topic = documentMetadata.getTopic();

        Method method = documentMetadata.getIdMetadata().getMethod();
        try {
            String key = String.valueOf(method.invoke(value));
            send(topic, key, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JkesException(
                    String.format("Can't invoke method[%s] on object[%s] of class[%s]", method, value, value.getClass()),
                    e);
        }
    }

    public Future<RecordMetadata> send(Object value, Callback callback) {
        DocumentMetadata documentMetadata =
                Metadata.getMetadata().getMetadataMap().get(value.getClass());

        String topic = documentMetadata.getTopic();

        Method method = documentMetadata.getIdMetadata().getMethod();
        try {
            String key = String.valueOf(method.invoke(value));
            return producer.send(new ProducerRecord<>(topic, key, value), callback);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JkesException(
                    String.format("Can't invoke method[%s] on object[%s] of class[%s]", method, value, value.getClass()),
                    e);
        }
    }

    public void send(Iterable<?> iterable) {
        iterable.forEach(this::send);
    }

    public Iterable<Future<RecordMetadata>> send(Iterable<?> records, Callback callback) {
        LinkedList<Future<RecordMetadata>> futures = new LinkedList<>();
        records.forEach(item -> futures.add(this.send(item, callback)));

        return futures;
    }

    public void send(Object[] records) {
        for (Object record : records) {
            send(record);
        }
    }

    public Iterable<Future<RecordMetadata>> send(Object[] records, Callback callback) {
        LinkedList<Future<RecordMetadata>> futures = new LinkedList<>();
        for (Object record : records) {
            futures.add(send(record, callback));
        }

        return futures;
    }

    public void send(String topic, String key, Object value) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, value);
        producer.send(record);
    }

    public Future<RecordMetadata> send(String topic, String key, Object value, Callback callback) {
        return producer.send(new ProducerRecord<>(topic, key, value), callback);
    }

    @PreDestroy
    public void close() {
        producer.close();
    }
}
