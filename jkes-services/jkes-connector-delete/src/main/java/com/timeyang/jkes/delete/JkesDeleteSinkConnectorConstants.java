/**
 * Copyright 2016 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 **/

package com.timeyang.jkes.delete;

import org.apache.kafka.connect.data.Schema.Type;

import java.util.HashMap;
import java.util.Map;

public class JkesDeleteSinkConnectorConstants {
  public static final String MAP_KEY = "key";
  public static final String MAP_VALUE = "value";

  public static final String BOOLEAN_TYPE = "boolean";
  public static final String BYTE_TYPE = "byte";
  public static final String BINARY_TYPE = "binary";
  public static final String SHORT_TYPE = "short";
  public static final String INTEGER_TYPE = "integer";
  public static final String LONG_TYPE = "long";
  public static final String FLOAT_TYPE = "float";
  public static final String DOUBLE_TYPE = "double";
  public static final String STRING_TYPE = "string";
  public static final String DATE_TYPE = "date";

  public static final Map<Type, String> TYPES = new HashMap<>();

  static {
    TYPES.put(Type.BOOLEAN, BOOLEAN_TYPE);
    TYPES.put(Type.INT8, BYTE_TYPE);
    TYPES.put(Type.INT16, SHORT_TYPE);
    TYPES.put(Type.INT32, INTEGER_TYPE);
    TYPES.put(Type.INT64, LONG_TYPE);
    TYPES.put(Type.FLOAT32, FLOAT_TYPE);
    TYPES.put(Type.FLOAT64, DOUBLE_TYPE);
    TYPES.put(Type.STRING, STRING_TYPE);
    TYPES.put(Type.BYTES, BINARY_TYPE);
  }
}
