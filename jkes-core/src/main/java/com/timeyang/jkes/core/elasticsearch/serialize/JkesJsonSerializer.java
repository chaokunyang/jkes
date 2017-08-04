package com.timeyang.jkes.core.elasticsearch.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.timeyang.jkes.core.elasticsearch.exception.IllegalAnnotatedFieldException;
import com.timeyang.jkes.core.util.StringUtils;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.core.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * entity capable of search, json serializer
 *
 * @author chaokunyang
 */
public class JkesJsonSerializer<T> extends StdSerializer<T> {

    public JkesJsonSerializer() {
        this(null);
    }

    protected JkesJsonSerializer(Class<T> t) {
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
        Method[] methods = entityClass.getMethods(); // include method inherited from super classes
        for(Method method : methods) {
            // check whether serialization will be recursive
            if (willRecursive(gen, context, method)) {
                gen.writeEndObject();
                return;
            }

            // method annotated with @Field
            Field fieldAnnotation = method.getAnnotation(Field.class);
            if(fieldAnnotation != null) {
                serializeField(value, gen, entityClass, method, fieldAnnotation);
            }else {
                MultiFields multiFields = method.getAnnotation(MultiFields.class);
                if(multiFields != null) {
                    Field mainFieldAnnotation = multiFields.mainField();
                    serializeField(value, gen, entityClass, method, mainFieldAnnotation);
                }
            }
        }

        // gen.writeObjectField("extra_field", "whatever_value");
        gen.writeEndObject();

    }

    private void serializeField(T value, JsonGenerator gen, Class<?> entityClass, Method method, Field fieldAnnotation) throws IOException {
        try {
            String key;
            if (StringUtils.hasLength(fieldAnnotation.name())) {
                key = StringUtils.addUnderscores(StringUtils.trimAllWhitespace(fieldAnnotation.name()));
            }else {
                String methodName = method.getName();
                if(methodName.startsWith("is")) {
                    key = StringUtils.addUnderscores(methodName.substring(2));
                }else if(methodName.startsWith("get")) {
                    key = StringUtils.addUnderscores(method.getName().substring(3));
                }else {
                    throw new IllegalAnnotatedFieldException(Field.class.getCanonicalName() +
                    "can only be annotated on getter method or isXXX method, " + "can't be annotated on method on " +
                    entityClass.getCanonicalName() + "." + methodName);
                }
            }

            Object v;
            try {
                v = method.invoke(value);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            // serialize data
            gen.writeObjectField(key, v);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean willRecursive(JsonGenerator gen, JsonStreamContext ctxt, Method method) throws IOException {
        if (ctxt != null) {
            JsonStreamContext ptxt = ctxt.getParent();
            while(ptxt != null) {
                // if(ctxt.getCurrentValue() != ptxt.getCurrentValue()) {
                //     ptxt = ptxt.getParent();
                // }else {
                //     gen.writeEndObject();
                //     return;
                // }

                // if(ptxt.getCurrentValue() == null || !ctxt.getCurrentValue().getClass().equals(ptxt.getCurrentValue().getClass())) {
                //         ptxt = ptxt.getParent();
                // }else {
                //     gen.writeEndObject();
                //     return;
                // }

                // String typeName = method.getGenericReturnType().getTypeName();
                if(ptxt.getCurrentValue() == null || !ReflectionUtils.getInnermostType(method).equals(ptxt.getCurrentValue().getClass().getCanonicalName())) {
                    ptxt = ptxt.getParent();
                }else {
                    return true;
                }
            }
        }
        return false;
    }
}
