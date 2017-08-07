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

import com.timeyang.jkes.delete.bulk.BulkProcessor;
import io.searchbox.client.JestClient;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JkesDocumentDeleter {
  private static final Logger log = LoggerFactory.getLogger(JkesDocumentDeleter.class);

  private final JestClient client;
  private final boolean ignoreSchema;
  private final Set<String> ignoreSchemaTopics;
  private final String versionType;
  private final long flushTimeoutMs;
  private final BulkProcessor<DeletableRecord, ?> bulkProcessor;

  private final Set<String> existingMappings;

  JkesDocumentDeleter(
          JestClient client,
          boolean ignoreSchema,
          Set<String> ignoreSchemaTopics,
          String versionType,
          long flushTimeoutMs,
          int maxBufferedRecords,
          int maxInFlightRequests,
          int batchSize,
          long lingerMs,
          int maxRetries,
          long retryBackoffMs
  ) {
    this.client = client;
    this.ignoreSchema = ignoreSchema;
    this.ignoreSchemaTopics = ignoreSchemaTopics;
    this.versionType = versionType;
    this.flushTimeoutMs = flushTimeoutMs;

    bulkProcessor = new BulkProcessor<>(
            new SystemTime(),
            new BulkDeletingClient(client),
            maxBufferedRecords,
            maxInFlightRequests,
            batchSize,
            lingerMs,
            maxRetries,
            retryBackoffMs
    );

    existingMappings = new HashSet<>();
  }

  public static class Builder {
    private final JestClient client;
    private String type;
    private boolean ignoreKey = false;
    private Set<String> ignoreKeyTopics = Collections.emptySet();
    private boolean ignoreSchema = false;
    private Set<String> ignoreSchemaTopics = Collections.emptySet();
    private String versionType;

    private long flushTimeoutMs;
    private int maxBufferedRecords;
    private int maxInFlightRequests;
    private int batchSize;
    private long lingerMs;
    private int maxRetry;
    private long retryBackoffMs;

    public Builder(JestClient client) {
      this.client = client;
    }

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public Builder setIgnoreKey(boolean ignoreKey, Set<String> ignoreKeyTopics) {
      this.ignoreKey = ignoreKey;
      this.ignoreKeyTopics = ignoreKeyTopics;
      return this;
    }

    public Builder setIgnoreSchema(boolean ignoreSchema, Set<String> ignoreSchemaTopics) {
      this.ignoreSchema = ignoreSchema;
      this.ignoreSchemaTopics = ignoreSchemaTopics;
      return this;
    }

    public Builder setVersionType(String versionType) {
      this.versionType = versionType;
      return this;
    }

    public Builder setFlushTimoutMs(long flushTimeoutMs) {
      this.flushTimeoutMs = flushTimeoutMs;
      return this;
    }

    public Builder setMaxBufferedRecords(int maxBufferedRecords) {
      this.maxBufferedRecords = maxBufferedRecords;
      return this;
    }

    public Builder setMaxInFlightRequests(int maxInFlightRequests) {
      this.maxInFlightRequests = maxInFlightRequests;
      return this;
    }

    public Builder setBatchSize(int batchSize) {
      this.batchSize = batchSize;
      return this;
    }

    public Builder setLingerMs(long lingerMs) {
      this.lingerMs = lingerMs;
      return this;
    }

    public Builder setMaxRetry(int maxRetry) {
      this.maxRetry = maxRetry;
      return this;
    }

    public Builder setRetryBackoffMs(long retryBackoffMs) {
      this.retryBackoffMs = retryBackoffMs;
      return this;
    }

    public JkesDocumentDeleter build() {
      return new JkesDocumentDeleter(
              client,
              ignoreSchema,
              ignoreSchemaTopics,
              versionType,
              flushTimeoutMs,
              maxBufferedRecords,
              maxInFlightRequests,
              batchSize,
              lingerMs,
              maxRetry,
              retryBackoffMs
      );
    }
  }

  public void delete(Collection<SinkRecord> records) {
    for (SinkRecord sinkRecord : records) {
      final DeletableRecord deletableRecord = DataConverter.convertRecord(sinkRecord, ignoreSchema, versionType);

      if (deletableRecord != null)
        bulkProcessor.add(deletableRecord, flushTimeoutMs);
    }
  }

  public void flush() {
    bulkProcessor.flush(flushTimeoutMs);
  }

  public void start() {
    bulkProcessor.start();
  }

  public void stop() {
    try {
      bulkProcessor.flush(flushTimeoutMs);
    } catch (Exception e) {
      log.warn("Failed to flush during stop", e);
    }
    bulkProcessor.stop();
    bulkProcessor.awaitStop(flushTimeoutMs);
  }

}
