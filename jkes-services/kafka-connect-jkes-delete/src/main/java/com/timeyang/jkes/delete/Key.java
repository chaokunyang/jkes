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

import java.util.Objects;

public class Key {

  public final String index;
  public final String type;
  public final String id;

  public Key(String index, String type, String id) {
    this.index = index;
    this.type = type;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Key that = (Key) o;
    return Objects.equals(index, that.index) &&
           Objects.equals(type, that.type) &&
           Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, type, id);
  }

  @Override
  public String toString() {
    return String.format("Key{%s/%s/%s}", index, type, id);
  }

}
