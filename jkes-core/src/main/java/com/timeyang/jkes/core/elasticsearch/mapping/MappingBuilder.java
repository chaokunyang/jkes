package com.timeyang.jkes.core.elasticsearch.mapping;

import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import com.timeyang.jkes.core.annotation.InnerField;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.core.annotation.NotThreadSafe;
import com.timeyang.jkes.core.exception.FieldTypeInferException;
import com.timeyang.jkes.core.metadata.DocumentMetadata;
import com.timeyang.jkes.core.metadata.FieldMetadata;
import com.timeyang.jkes.core.metadata.Metadata;
import com.timeyang.jkes.core.metadata.MultiFieldsMetadata;
import com.timeyang.jkes.core.util.ReflectionUtils;
import com.timeyang.jkes.core.util.StringUtils;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * <p>generate mapping base on search annotation on entity</p>
 *
 * This implement is inspired by <i>org.springframework.data.elasticsearch.core.MappingBuilder</i>, but is more complicated and not use elasticsearch java client api
 *
 * <p>Some agreement is provided: </p>
 * <p>
 *     "dynamic", "strict"
 * </p>
 * <p>If new fields are detected, an exception is thrown and the document is rejected.
 disable dynamically adding field for document and inner object
 (inherit the setting from their parent object or from the mapping type).
 to make application annotation become only source of truth for mapping in elasticsearch</p>
 *
 * <p>Note this class must be created for each document</p>
 * @author chaokunyang
 */
@NotThreadSafe
public class MappingBuilder {

    private static final Set<String> primitives = new HashSet<>(8);
    static {
        primitives.add("byte");
        primitives.add("short");
        primitives.add("int");
        primitives.add("long");
        primitives.add("float");
        primitives.add("double");
        primitives.add("boolean");
        primitives.add("char");
    }

    private static final String FIELD_TYPE = "type";
    private static final String FIELD_STORE = "store";
    private static final String FIELD_ANALYZER = "analyzer";

    private static final String FIELD_PROPERTIES = "properties";

    private LinkedList<Class<?>> context = new LinkedList<>();

    public JSONObject buildMapping(Class<?> clazz) {

        JSONObject mapping = new JSONObject();
        // If new fields are detected, an exception is thrown and the document is rejected.
        // disable dynamically adding field for document and inner object
        // (inherit the setting from their parent object or from the mapping type).
        // to make application annotation become only source of truth for mapping in elasticsearch
        mapping.put("dynamic", "strict");
        mapping.put(FIELD_PROPERTIES, getEntityMappingProperties(clazz));

        return mapping;
    }

    /**
     * <p>Generate properties json for specified entity.</p>
     * <em>If field of entity will cause recursion, it will be ignored</em>
     * @param clazz entity class or return type of complex field annotated method
     * @return properties for class
     */
    private JSONObject getEntityMappingProperties(Class<?> clazz) {
        context.addLast(clazz);

        JSONObject properties = new JSONObject();

        DocumentMetadata documentMetadata = Metadata.getMetadata().getMetadataMap().get(clazz);
        Set<FieldMetadata> fieldMetadataSet = documentMetadata.getFieldMetadataSet();
        Set<MultiFieldsMetadata> multiFieldsMetadataSet = documentMetadata.getMultiFieldsMetadataSet();

        for(FieldMetadata fieldMetadata : fieldMetadataSet) {
            Method method = fieldMetadata.getMethod();
            if(!willRecursive(method))
                properties.put(fieldMetadata.getFieldName(),
                    getSingleFieldMapping(method, fieldMetadata.getField()));
        }

        for (MultiFieldsMetadata multiFieldsMetadata : multiFieldsMetadataSet) {
            Method method = multiFieldsMetadata.getMethod();
            if(!willRecursive(method))
                properties.put(multiFieldsMetadata.getFieldName(),
                    getMultiFieldsMapping(multiFieldsMetadata.getMethod(), multiFieldsMetadata.getMultiFields()));
        }

        context.removeLast();

        return properties;
    }

    /**
     * check whether or not recursive by iterate context to check if any repetitive class object
     * @param method the field to be checked
     * @return recursive or not
     */
    private boolean willRecursive(Method method) {

        String returnTypeName = ReflectionUtils.getInnermostType(method);
        if(primitives.contains(returnTypeName)) return false;

        Class<?> returnType;
        try {
            returnType = Class.forName(returnTypeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't found class for return type: " + returnTypeName + " in " + method); // not possible
        }

        boolean willRecursive = false;
        for(int i = context.size() - 1; i >= 0; i--) {
            if(context.get(i) == returnType) {
                willRecursive = true;
                break;
            }
        }

        return willRecursive;
    }

    private JSONObject getSingleFieldMapping(Method method, Field field) {
        FieldType fieldType;
        if(field.type() == FieldType.Auto) {
            fieldType = inferFieldType(method);
        }else {
            fieldType = field.type();
        }

        if(fieldType == FieldType.Object) {
            return getObjectFieldMapping(method);
        }else if(fieldType == FieldType.Nested) {
            return getNestedFieldMapping(method);
        }else {
            return getPlainSingleFieldMapping(field, fieldType);
        }

    }

    private JSONObject getPlainSingleFieldMapping(Field field, FieldType fieldType) {
        JSONObject fieldMapping = new JSONObject();

        fieldMapping.put(FIELD_TYPE, fieldType.toString().toLowerCase());

        if(StringUtils.hasText(field.analyzer())) {
            fieldMapping.put(FIELD_ANALYZER, field.analyzer());
        }

        fieldMapping.put(FIELD_STORE, field.store());

        return fieldMapping;
    }

    private JSONObject getObjectFieldMapping(Method method) {
        JSONObject fieldMapping = new JSONObject();

        String returnTypeName = ReflectionUtils.getInnermostType(method);
        Class<?> returnType;
        try {
            returnType = Class.forName(returnTypeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't found class for return type: " + returnTypeName + " in " + method); // not possible
        }

        // 需要判断父级对象有没有重复的，不然会引起递归
        fieldMapping.put("properties", getEntityMappingProperties(returnType));

        return fieldMapping;
    }

    private JSONObject getNestedFieldMapping(Method method) {
        JSONObject fieldMapping = new JSONObject();
        fieldMapping.put(FIELD_TYPE, "nested");

        String returnTypeName = ReflectionUtils.getInnermostType(method);
        Class<?> returnType;
        try {
            returnType = Class.forName(returnTypeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't found class for return type: " + returnTypeName + " in " + method); // not possible
        }

        // 需要判断父级对象有没有重复的，不然会引起递归。使用栈数据类型进行判断 Dqueue


        fieldMapping.put("properties", getEntityMappingProperties(returnType));

        return fieldMapping;
    }

    private JSONObject getMultiFieldsMapping(Method method, MultiFields multiFields) {
        JSONObject fieldMapping;
        Field mainField = multiFields.mainField();
        fieldMapping = getSingleFieldMapping(method, mainField);

        JSONObject otherFieldsMapping = new JSONObject();
        fieldMapping.put("fields", otherFieldsMapping);
        InnerField[] otherFields = multiFields.otherFields();
        for(InnerField innerField : otherFields) {
            otherFieldsMapping.put(innerField.suffix(), getInnerFieldMapping(method, innerField));
        }

        return fieldMapping;
    }

    JSONObject getInnerFieldMapping(Method method, InnerField innerField) {
        JSONObject fieldMapping = new JSONObject();

        FieldType fieldType;
        if(innerField.type() == FieldType.Auto) {
            fieldType = inferFieldType(method);
        }else {
            fieldType = innerField.type();
        }
        fieldMapping.put(FIELD_TYPE, fieldType.toString().toLowerCase());

        if(StringUtils.hasText(innerField.analyzer())) {
            fieldMapping.put(FIELD_ANALYZER, innerField.analyzer());
        }

        fieldMapping.put(FIELD_STORE, innerField.store());

        return fieldMapping;
    }


    static FieldType inferFieldType(Method method) {
        FieldType fieldType = null;
        String returnType = ReflectionUtils.getInnermostType(method);
        if (returnType.matches("byte|.*Byte$")) {
            fieldType = FieldType.Byte;
        }else if(returnType.matches("short|.*Short$")) {
            fieldType = FieldType.Short;
        }else if(returnType.matches("int|.*Integer$")) {
            fieldType = FieldType.Integer;
        }else if(returnType.matches("long|.*Long$")) {
            fieldType = FieldType.Long;
        }else if(returnType.matches("float|.*Float$")) {
            fieldType = FieldType.Float;
        }else if(returnType.matches("double|.*Double$")) {
            fieldType = FieldType.Double;
        }else if(returnType.matches("boolean|.*Boolean$")) {
            fieldType = FieldType.Boolean;
        }else if(returnType.matches(".*Date$")) {
            fieldType = FieldType.Date;
        }

        if(fieldType == null) {
            throw new FieldTypeInferException("Can't infer field type for method: " + method + " , please specify field type manually");
        }

        return fieldType;
    }

}
