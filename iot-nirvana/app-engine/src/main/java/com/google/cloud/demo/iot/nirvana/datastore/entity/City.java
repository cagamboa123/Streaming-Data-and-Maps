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

package com.google.cloud.demo.iot.nirvana.datastore.entity;

import com.google.cloud.demo.iot.nirvana.shared.Utils;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.security.MessageDigest;

/** Class that represents Datastore entity describing a city */
@Entity
public class City {

  @Id String id;
  String name;
  double lat;
  double lng;
  double temperature; // this is the average temperature of the city during the year

  public City() {
    ;
  }

  /**
   * General constructor
   *
   * @param name the name of the city
   * @param lat latitude of the city
   * @param lng longitude of the city
   */
  public City(String name, double lat, double lng, double temperature) {

    // generate ID as MD5(lat+"-"+lng)
    try {
      String id = new String(lat + "-" + lng);
      MessageDigest md = MessageDigest.getInstance("MD5");
      this.id = Utils.toHexString(md.digest(id.getBytes("UTF-8")));
    } catch (Exception ex) {
      ;
    }

    this.lat = lat;
    this.lng = lng;
    this.name = name;
    this.temperature = temperature;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
}
