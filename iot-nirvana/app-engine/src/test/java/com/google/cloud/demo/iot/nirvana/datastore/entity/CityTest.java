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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link City}. */
@RunWith(JUnit4.class)
public final class CityTest {

  @Test
  public void constructor_resultOk() throws Exception {
    City city = new City("Paris", 41.3275459, 19.81869819999997, 31.0);
    assertEquals(city.getId(), "8762723AE028CAA144CBF8B7069003C3");
    assertEquals(city.getName(), "Paris");
    assertTrue(city.getLat() == 41.3275459);
    assertTrue(city.getLng() == 19.81869819999997);
    assertTrue(city.getTemperature() == 31.0);
  }

  @Test
  public void setId_resultOk() throws Exception {
    City city = new City();
    city.setId("8762723AE028CAA144CBF8B7069003C3");
    assertEquals(city.getId(), "8762723AE028CAA144CBF8B7069003C3");
  }

  @Test
  public void setName_resultOk() throws Exception {
    City city = new City();
    city.setName("Paris");
    assertEquals(city.getName(), "Paris");
  }

  @Test
  public void setTemperature_resultOkPositive() throws Exception {
    City city = new City();
    city.setTemperature(35.8);
    assertTrue(city.getTemperature() == 35.8);
  }

  @Test
  public void setTemperature_resultOkNegative() throws Exception {
    City city = new City();
    city.setTemperature(-35.8);
    assertTrue(city.getTemperature() == -35.8);
  }

  @Test
  public void setLat_resultOkPositive() throws Exception {
    City city = new City();
    city.setLat(41.3275459);
    assertTrue(city.getLat() == 41.3275459);
  }

  @Test
  public void setLat_resultOkNegative() throws Exception {
    City city = new City();
    city.setLat(-41.3275459);
    assertTrue(city.getLat() == -41.3275459);
  }

  @Test
  public void setLng_resultOkPositive() throws Exception {
    City city = new City();
    city.setLng(19.81869819999997);
    assertTrue(city.getLng() == 19.81869819999997);
  }

  @Test
  public void setLng_resultOkNegative() throws Exception {
    City city = new City();
    city.setLng(-70.0086306);
    assertTrue(city.getLng() == -70.0086306);
  }
}
