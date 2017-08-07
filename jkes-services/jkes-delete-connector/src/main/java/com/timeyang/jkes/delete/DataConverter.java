/**
 * Copyright 2016 Confluent Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 **/

package com.timeyang.jkes.delete;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Time;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.storage.Converter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class DataConverter {

  private static final Converter JSON_CONVERTER;
  private static final Gson GSON = new Gson();

  static {
    JSON_CONVERTER = new JsonConverter();
    JSON_CONVERTER.configure(Collections.singletonMap("schemas.enable", "false"), false);
  }


  public static DeletableRecord convertRecord(SinkRecord record, boolean ignoreSchema, String versionType) {
    final Schema schema;
    final Object value;
    if (!ignoreSchema) {
      schema = preProcessSchema(record.valueSchema());
      value = preProcessValue(record.value(), record.valueSchema(), schema);
    } else {
      schema = record.valueSchema();
      value = record.value();
    }

    final String payload = new String(JSON_CONVERTER.fromConnectData(record.topic(), schema, value), StandardCharsets.UTF_8);

    if (StringUtils.isNotBlank(payload)) {
      DeleteEvent deleteEvent = GSON.fromJson(payload, DeleteEvent.class);
      return new DeletableRecord(new Key(deleteEvent.getIndex(), deleteEvent.getType(), deleteEvent.getId()), deleteEvent.getVersion(), versionType);
    } else {
      return null;
    }

  }


  // We need to pre process the Kafka Connect schema before converting to JSON as Elasticsearch
  // expects a different JSON format from the current JSON converter provides. Rather than completely
  // rewrite a converter for Elasticsearch, we will refactor the JSON converter to support customized
  // translation. The pre process is no longer needed once we have the JSON converter refactored.
  static Schema preProcessSchema(Schema schema) {
    if (schema == null) {
      return null;
    }
    // Handle logical types
    String schemaName = schema.name();
    if (schemaName != null) {
      switch (schemaName) {
        case Decimal.LOGICAL_NAME:
          return copySchemaBasics(schema, SchemaBuilder.float64()).build();
        case Date.LOGICAL_NAME:
        case Time.LOGICAL_NAME:
        case Timestamp.LOGICAL_NAME:
          return schema;
      }
    }

    Schema.Type schemaType = schema.type();
    switch (schemaType) {
      case ARRAY: {
        return copySchemaBasics(schema, SchemaBuilder.array(preProcessSchema(schema.valueSchema()))).build();
      }
      case MAP: {
        Schema keySchema = schema.keySchema();
        Schema valueSchema = schema.valueSchema();
        String keyName = keySchema.name() == null ? keySchema.type().name() : keySchema.name();
        String valueName = valueSchema.name() == null ? valueSchema.type().name() : valueSchema.name();
        Schema elementSchema = SchemaBuilder.struct().name(keyName + "-" + valueName)
                .field(JkesDeleteSinkConnectorConstants.MAP_KEY, preProcessSchema(keySchema))
                .field(JkesDeleteSinkConnectorConstants.MAP_VALUE, preProcessSchema(valueSchema))
                .build();
        return copySchemaBasics(schema, SchemaBuilder.array(elementSchema)).build();
      }
      case STRUCT: {
        SchemaBuilder structBuilder = copySchemaBasics(schema, SchemaBuilder.struct().name(schemaName));
        for (Field field : schema.fields()) {
          structBuilder.field(field.name(), preProcessSchema(field.schema()));
        }
        return structBuilder.build();
      }
      default: {
        return schema;
      }
    }
  }

  private static SchemaBuilder copySchemaBasics(Schema source, SchemaBuilder target) {
    if (source.isOptional()) {
      target.optional();
    }
    if (source.defaultValue() != null && source.type() != Schema.Type.STRUCT) {
      final Object preProcessedDefaultValue = preProcessValue(source.defaultValue(), source, target);
      target.defaultValue(preProcessedDefaultValue);
    }
    return target;
  }

  // visible for testing
  static Object preProcessValue(Object value, Schema schema, Schema newSchema) {
    if (schema == null) {
      return value;
    }
    if (value == null) {
      if (schema.defaultValue() != null) {
        return schema.defaultValue();
      }
      if (schema.isOptional()) {
        return null;
      }
      throw new DataException("null value for field that is required and has no default value");
    }

    // Handle logical types
    String schemaName = schema.name();
    if (schemaName != null) {
      switch (schemaName) {
        case Decimal.LOGICAL_NAME:
          return ((BigDecimal) value).doubleValue();
        case Date.LOGICAL_NAME:
        case Time.LOGICAL_NAME:
        case Timestamp.LOGICAL_NAME:
          return value;
      }
    }

    Schema.Type schemaType = schema.type();
    Schema keySchema;
    Schema valueSchema;
    switch (schemaType) {
      case ARRAY:
        Collection collection = (Collection) value;
        ArrayList<Object> result = new ArrayList<>();
        for (Object element : collection) {
          result.add(preProcessValue(element, schema.valueSchema(), newSchema.valueSchema()));
        }
        return result;
      case MAP:
        keySchema = schema.keySchema();
        valueSchema = schema.valueSchema();
        ArrayList<Struct> mapStructs = new ArrayList<>();
        Map<?, ?> map = (Map<?, ?>) value;
        Schema newValueSchema = newSchema.valueSchema();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          Struct mapStruct = new Struct(newValueSchema);
          mapStruct.put(JkesDeleteSinkConnectorConstants.MAP_KEY, preProcessValue(entry.getKey(), keySchema, newValueSchema.field(JkesDeleteSinkConnectorConstants.MAP_KEY).schema()));
          mapStruct.put(JkesDeleteSinkConnectorConstants.MAP_VALUE, preProcessValue(entry.getValue(), valueSchema, newValueSchema.field(JkesDeleteSinkConnectorConstants.MAP_VALUE).schema()));
          mapStructs.add(mapStruct);
        }
        return mapStructs;
      case STRUCT:
        Struct struct = (Struct) value;
        Struct newStruct = new Struct(newSchema);
        for (Field field : schema.fields()) {
          Object converted = preProcessValue(struct.get(field), field.schema(), newSchema.field(field.name()).schema());
          newStruct.put(field.name(), converted);
        }
        return newStruct;
      default:
        return value;
    }
  }
}
