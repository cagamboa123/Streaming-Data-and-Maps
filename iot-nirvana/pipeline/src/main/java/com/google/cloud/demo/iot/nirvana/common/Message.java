/*
 * Copyright (C) 2018 Google Inc.
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
 */

package com.google.cloud.demo.iot.nirvana.common;

import java.io.Serializable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

/**
 * Java POJO carrying a temperature measurement.
 */
@DefaultCoder(AvroCoder.class)
public class Message implements Serializable {

  String id;
  double temperature;
  long timestamp;

  public Message() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public boolean equals(Object o) {
    if (o == null || !(o instanceof Message)) {
      return false;
    } else {
      Message m = (Message) o;
      boolean isEqual =
          (this.getId() != null && m.getId() != null && this.getId().equals(m.getId()))
          && this.getTemperature() == m.getTemperature()
          && this.getTimestamp() == m.getTimestamp();
      return isEqual;
    }
  }
}
