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

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

import java.util.Map;

public class JkesDeleteSinkConnectorConfig extends AbstractConfig {

  public static final String CONNECTION_URL_CONFIG = "connection.url";
  public static final String BATCH_SIZE_CONFIG = "batch.size";
  public static final String MAX_IN_FLIGHT_REQUESTS_CONFIG = "max.in.flight.requests";
  public static final String MAX_BUFFERED_RECORDS_CONFIG = "max.buffered.records";
  public static final String LINGER_MS_CONFIG = "linger.ms";
  public static final String FLUSH_TIMEOUT_MS_CONFIG = "flush.timeout.ms";
  public static final String MAX_RETRIES_CONFIG = "max.retries";

  public static final String RETRY_BACKOFF_MS_CONFIG = "retry.backoff.ms";
  public static final String SCHEMA_IGNORE_CONFIG = "schema.ignore";
  public static final String TOPIC_SCHEMA_IGNORE_CONFIG = "topic.schema.ignore";

  public static final String VERSION_TYPE_CONFIG = "version.type";

  protected static ConfigDef baseConfigDef() {
    final ConfigDef configDef = new ConfigDef();

    {
      final String group = "Connector";
      int order = 0;
      configDef
          .define(CONNECTION_URL_CONFIG, Type.LIST, Importance.HIGH,
                  "List of Elasticsearch HTTP connection URLs e.g. ``http://eshost1:9200,http://eshost2:9200``.",
                  group, ++order, Width.LONG, "Connection URLs")
          .define(BATCH_SIZE_CONFIG, Type.INT, 2000, Importance.MEDIUM,
                  "The number of records to process as a batch when writing to Elasticsearch.",
                  group, ++order, Width.SHORT, "Batch Size")
          .define(MAX_IN_FLIGHT_REQUESTS_CONFIG, Type.INT, 5, Importance.MEDIUM,
                  "The maximum number of indexing requests that can be in-flight to Elasticsearch before blocking further requests.",
                  group, 5, Width.SHORT, "Max In-flight Requests")
          .define(MAX_BUFFERED_RECORDS_CONFIG, Type.INT, 20000, Importance.LOW,
                  "The maximum number of records each task will buffer before blocking acceptance of more records. This config can be used to limit the memory usage for each task.",
                  group, ++order, Width.SHORT, "Max Buffered Records")
          .define(LINGER_MS_CONFIG, Type.LONG, 1L, Importance.LOW,
                  "Linger time in milliseconds for batching.\n"
                  + "Records that arrive in between request transmissions are batched into a single bulk indexing request, based on the ``" + BATCH_SIZE_CONFIG + "`` configuration. "
                  + "Normally this only occurs under load when records arrive faster than they can be sent out. "
                  + "However it may be desirable to reduce the number of requests even under light load and benefit from bulk indexing. "
                  + "This setting helps accomplish that - when a pending batch is not full, rather than immediately sending it out "
                  + "the task will wait up to the given delay to allow other records to be added so that they can be batched into a single request.",
                  group, ++order, Width.SHORT, "Linger (ms)")
          .define(FLUSH_TIMEOUT_MS_CONFIG, Type.LONG, 10000L, Importance.LOW,
                  "The timeout in milliseconds to use for periodic flushing, "
                  + "and when waiting for buffer space to be made available by completed requests as records are added. "
                  + "If this timeout is exceeded the task will fail.",
                  group, ++order, Width.SHORT, "Flush Timeout (ms)")
          .define(MAX_RETRIES_CONFIG, Type.INT, 5, Importance.LOW,
                  "The maximum number of retries that are allowed for failed indexing requests. "
                  + "If the retry attempts are exhausted the task will fail.",
                  group, ++order, Width.SHORT, "Max Retries")
          .define(RETRY_BACKOFF_MS_CONFIG, Type.LONG, 100L, Importance.LOW,
                  "How long to wait in milliseconds before attempting to retry a failed indexing request. "
                  + "This avoids retrying in a tight loop under failure scenarios.",
                  group, ++order, Width.SHORT, "Retry Backoff (ms)");
    }

    {
      final String group = "Data Conversion";
      int order = 0;
      configDef
          .define(VERSION_TYPE_CONFIG, Type.STRING, "external", Importance.HIGH,
                  "Document version type. Mostly use \"external\". Sometimes \"external_gte\" is also useful, but should be used with care. If used incorrectly, it can result in loss of data.",
                  group, ++order, Width.MEDIUM, "Document version field")
          .define(SCHEMA_IGNORE_CONFIG, Type.BOOLEAN, false, Importance.LOW,
                  "Whether to ignore schemas during indexing. "
                  + "When this is set to ``true``, the record schema will be ignored for the purpose of registering an Elasticsearch mapping. "
                  + "Elasticsearch will infer the mapping from the data (dynamic mapping needs to be enabled by the user).\n"
                  + "Note that this is a global config that applies to all topics, use ``" + TOPIC_SCHEMA_IGNORE_CONFIG + "`` to override as ``true`` for specific topics.",
                  group, ++order, Width.SHORT, "Ignore Schema mode")
          .define(TOPIC_SCHEMA_IGNORE_CONFIG, Type.LIST, "", Importance.LOW,
                  "List of topics for which ``" + SCHEMA_IGNORE_CONFIG + "`` should be ``true``.",
                  group, ++order, Width.LONG, "Topics for 'Ignore Schema' mode");
    }

    return configDef;
  }

  public static final ConfigDef CONFIG = baseConfigDef();

  public JkesDeleteSinkConnectorConfig(Map<String, String> props) {
    super(CONFIG, props);
  }

  public static void main(String[] args) {
    System.out.println(CONFIG.toEnrichedRst());
  }
}
