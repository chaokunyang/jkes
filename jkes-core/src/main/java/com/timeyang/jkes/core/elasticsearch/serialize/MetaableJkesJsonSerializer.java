package com.timeyang.jkes.core.elasticsearch.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.timeyang.jkes.core.exception.IllegalMemberAccessException;
import com.timeyang.jkes.core.exception.ReflectiveInvocationTargetException;
import com.timeyang.jkes.core.metadata.DocumentMetadata;
import com.timeyang.jkes.core.metadata.FieldMetadata;
import com.timeyang.jkes.core.metadata.Metadata;
import com.timeyang.jkes.core.metadata.MultiFieldsMetadata;
import com.timeyang.jkes.core.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Entity capable of search, json serializer
 *
 * @author chaokunyang
 */
public class MetaableJkesJsonSerializer<T> extends StdSerializer<T> {

    public MetaableJkesJsonSerializer() {
        this(null);
    }

    private MetaableJkesJsonSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public Class<T> handledType() {
        return _handledType;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        JsonStreamContext context = gen.getOutputContext();
        gen.setCurrentValue(value); // must set manually, or else the result of context.getCurrentValue() will be null。this method will call context.setCurrentValue(value);

        gen.writeStartObject(); // writeStartObject(value) will call setCurrentValue(value) well
        Class<?> entityClass = value.getClass();

        DocumentMetadata documentMetadata = Metadata.getMetadata().getMetadataMap().get(entityClass);
        Set<FieldMetadata> fieldMetadataSet = documentMetadata.getFieldMetadataSet();
        Set<MultiFieldsMetadata> multiFieldsMetadataSet = documentMetadata.getMultiFieldsMetadataSet();

        for(FieldMetadata fieldMetadata : fieldMetadataSet) {
            Method method = fieldMetadata.getMethod();
            // check whether serialization will be recursive
            if (willRecursive(context, method)) {
                gen.writeEndObject();
                return;
            }

            serializeField(value, gen, method, fieldMetadata.getFieldName());
        }

        for(MultiFieldsMetadata multiFieldsMetadata : multiFieldsMetadataSet) {
            Method method = multiFieldsMetadata.getMethod();
            // check whether serialization will be recursive
            if (willRecursive(context, method)) {
                gen.writeEndObject();
                return;
            }

            serializeField(value, gen, method, multiFieldsMetadata.getFieldName());
        }

        // gen.writeObjectField("extra_field", "whatever_value");
        gen.writeEndObject();
    }

    private void serializeField(T value, JsonGenerator gen, Method method, String fieldName) throws IOException {
        try {
            Object v = method.invoke(value);

            gen.writeObjectField(fieldName, v); // serialize data
        } catch (IllegalAccessException e) {
            throw new IllegalMemberAccessException(e);
        } catch (InvocationTargetException e) {
            throw new ReflectiveInvocationTargetException(e);
        }
    }

    private boolean willRecursive(JsonStreamContext ctxt, Method method) throws IOException {
        if (ctxt != null) {
            JsonStreamContext ptxt;
            ptxt = ctxt.getParent();
            while(ptxt != null) {
                if(ptxt.getCurrentValue() == null
                        || !ReflectionUtils.getInnermostType(method).equals(ptxt.getCurrentValue().getClass().getCanonicalName())) {
                    ptxt = ptxt.getParent();
                }else {
                    return true;
                }
            }
        }
        return false;
    }
}
