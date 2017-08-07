/**
 * Copyright 2017 timeyang.com .
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

import io.searchbox.core.Delete;

public class DeletableRecord {

  public final Key key;
  public final Long version;
  public final String versionType;

  public DeletableRecord(Key key, Long version, String versionType) {
    this.key = key;
    this.version = version;
    this.versionType = versionType;
  }

  public Delete toDeleteRequest() {
    Delete.Builder req = new Delete.Builder(key.id)
            .index(key.index)
            .type(key.type);
    if (version != null) {
      req.setParameter("version_type", versionType).setParameter("version", version);
    }
    return req.build();
  }

}
