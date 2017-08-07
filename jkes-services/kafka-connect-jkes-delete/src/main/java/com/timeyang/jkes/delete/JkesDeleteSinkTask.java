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

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JkesDeleteSinkTask extends SinkTask {

  private static final Logger log = LoggerFactory.getLogger(JkesDeleteSinkTask.class);
  private JkesDocumentDeleter writer;
  private JestClient client;

  @Override
  public String version() {
    return Version.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    start(props, null);
  }

  // public for testing
  public void start(Map<String, String> props, JestClient client) {
    try {
      log.info("Starting JkesDeleteSinkTask.");

      JkesDeleteSinkConnectorConfig config = new JkesDeleteSinkConnectorConfig(props);
      boolean ignoreSchema = config.getBoolean(JkesDeleteSinkConnectorConfig.SCHEMA_IGNORE_CONFIG);

      String versionType = config.getString(JkesDeleteSinkConnectorConfig.VERSION_TYPE_CONFIG);

      Set<String> topicIgnoreSchema =  new HashSet<>(config.getList(JkesDeleteSinkConnectorConfig.TOPIC_SCHEMA_IGNORE_CONFIG));

      long flushTimeoutMs = config.getLong(JkesDeleteSinkConnectorConfig.FLUSH_TIMEOUT_MS_CONFIG);
      int maxBufferedRecords = config.getInt(JkesDeleteSinkConnectorConfig.MAX_BUFFERED_RECORDS_CONFIG);
      int batchSize = config.getInt(JkesDeleteSinkConnectorConfig.BATCH_SIZE_CONFIG);
      long lingerMs = config.getLong(JkesDeleteSinkConnectorConfig.LINGER_MS_CONFIG);
      int maxInFlightRequests = config.getInt(JkesDeleteSinkConnectorConfig.MAX_IN_FLIGHT_REQUESTS_CONFIG);
      long retryBackoffMs = config.getLong(JkesDeleteSinkConnectorConfig.RETRY_BACKOFF_MS_CONFIG);
      int maxRetry = config.getInt(JkesDeleteSinkConnectorConfig.MAX_RETRIES_CONFIG);

      if (client != null) {
        this.client = client;
      } else {
        List<String> address = config.getList(JkesDeleteSinkConnectorConfig.CONNECTION_URL_CONFIG);
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(address).multiThreaded(true).build());
        this.client = factory.getObject();
      }

      JkesDocumentDeleter.Builder builder = new JkesDocumentDeleter.Builder(this.client)
          .setIgnoreSchema(ignoreSchema, topicIgnoreSchema)
          .setVersionType(versionType)
          .setFlushTimoutMs(flushTimeoutMs)
          .setMaxBufferedRecords(maxBufferedRecords)
          .setMaxInFlightRequests(maxInFlightRequests)
          .setBatchSize(batchSize)
          .setLingerMs(lingerMs)
          .setRetryBackoffMs(retryBackoffMs)
          .setMaxRetry(maxRetry);

      writer = builder.build();
      writer.start();
    } catch (ConfigException e) {
      throw new ConnectException("Couldn't start JkesDeleteSinkTask due to configuration error:", e);
    }
  }

  @Override
  public void open(Collection<TopicPartition> partitions) {
    log.debug("Opening the task for topic partitions: {}", partitions);
  }

  @Override
  public void put(Collection<SinkRecord> records) throws ConnectException {
    log.trace("Putting {} to Elasticsearch.", records);
    writer.delete(records);
  }

  @Override
  public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
    log.trace("Flushing data to Elasticsearch with the following offsets: {}", offsets);
    writer.flush();
  }

  @Override
  public void close(Collection<TopicPartition> partitions) {
    log.debug("Closing the task for topic partitions: {}", partitions);
  }

  @Override
  public void stop() throws ConnectException {
    log.info("Stopping JkesDeleteSinkTask.");
    if (writer != null) {
      writer.stop();
    }
    if (client != null) {
      client.shutdownClient();
    }
  }

}
