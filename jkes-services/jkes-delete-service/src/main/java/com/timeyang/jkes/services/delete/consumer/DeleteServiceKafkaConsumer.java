package com.timeyang.jkes.services.delete.consumer;

import com.timeyang.jkes.services.delete.domain.Event;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author chaokunyang
 */
public class DeleteServiceKafkaConsumer {

    private static DeleteServiceKafkaConsumer kafkaConsumer;

    KafkaConsumer<String, Event.DeleteEvent> consumer;

    private DeleteServiceKafkaConsumer(Properties props) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", props.getProperty("bootstrap.servers"));
        properties.put("group.id", props.getProperty("group.id"));
        properties.put("enable.auto.commit", false);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "io.confluent.kafka.serializers.KafkaJsonSerializer");

        this.consumer = new KafkaConsumer<>(properties);
        String topicsStr = (String) properties.get("subscribe");
        List<String> topics  = Arrays.asList(topicsStr.split(","));
        this.consumer.subscribe(topics);
    }

    public ConsumerRecords<String, Event.DeleteEvent> poll(long timeout) {
        return this.consumer.poll(timeout);
    }

    public void commitSync() {
        this.consumer.commitSync();
    }

    public void close() {
        this.consumer.close();
    }
}
