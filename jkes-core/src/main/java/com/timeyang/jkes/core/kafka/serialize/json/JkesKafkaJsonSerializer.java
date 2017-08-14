package com.timeyang.jkes.core.kafka.serialize.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.elasticsearch.serialize.MetaableJkesJsonSerializer;
import com.timeyang.jkes.core.util.ClassUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.Set;

/**
 * @author chaokunyang
 */
public class JkesKafkaJsonSerializer implements Serializer {
    private ObjectMapper mapper;

    public JkesKafkaJsonSerializer() {}

    @Override
    public void configure(Map configs, boolean isKey) {
        String documentBasePackage = (String) configs.get("documentBasePackage");

        SimpleModule module = new SimpleModule();
        Set<Class<?>> annotatedClasses = ClassUtils.getAnnotatedClasses(documentBasePackage, Document.class);
        annotatedClasses.forEach(aClass -> module.addSerializer(aClass, new MetaableJkesJsonSerializer<>()));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Feature is enabled by default, so that date/time are by default serialized as timestamps.
        mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);

        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(String topic, Object data) {
        if(data == null) {
            return null;
        } else {
            try {
                return this.mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        }
    }

    @Override
    public void close() {

    }
}
