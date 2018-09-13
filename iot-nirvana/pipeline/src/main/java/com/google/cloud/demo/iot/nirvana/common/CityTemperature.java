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
 * Java POJO carrying the temperature measured in a city.
 */
@DefaultCoder(AvroCoder.class)
public class CityTemperature implements Serializable {

  private String city;
  private double temperature;
  private double lat;
  private double lng;

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLng() {
    return lng;
  }

  public void setLng(double lng) {
    this.lng = lng;
  }

  public boolean equals(Object o) {
    if (o == null || !(o instanceof CityTemperature)) {
      return false;
    } else {
      CityTemperature c = (CityTemperature) o;
      boolean isEqual =
          (this.getCity() != null && c.getCity() != null && this.getCity().equals(c.getCity()))
          && this.getLat() == c.getLat()
          && this.getLng() == c.getLng()
          && this.getTemperature() == c.getTemperature();
      return isEqual;
    }
  }
}
