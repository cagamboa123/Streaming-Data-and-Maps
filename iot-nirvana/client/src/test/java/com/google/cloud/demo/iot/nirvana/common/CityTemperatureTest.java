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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CityTemperature}. */
@RunWith(JUnit4.class)
public final class CityTemperatureTest {

  private CityTemperature cityTirana1;
  private CityTemperature cityTirana2;
  private CityTemperature cityOranjestad;
  private CityTemperature cityNoName1;
  private CityTemperature cityNoName2;

    /** Create test data. */
  @Before
  public void createTestData() {
    Gson gson = new Gson();
    cityTirana1 = gson.fromJson(
        "{\"city\":\"Albania-Tirana\",\"temperature\":\"18\",\"lat\":41.3275459,\"lng\":19.81869819999997}",
        CityTemperature.class);
    cityTirana2 = gson.fromJson(
        "{\"city\":\"Albania-Tirana\",\"temperature\":\"18\",\"lat\":41.3275459,\"lng\":19.81869819999997}",
        CityTemperature.class);
    cityOranjestad = gson.fromJson(
        "{\"city\":\"Aruba-Oranjestad\",\"temperature\":\"31\",\"lat\":12.5092044,\"lng\":-70.0086306}",
        CityTemperature.class);
    cityNoName1 = gson.fromJson(
        "{\"temperature\":\"31\",\"lat\":12.5092044,\"lng\":-70.0086306}", CityTemperature.class);
    cityNoName2 = gson.fromJson(
        "{\"temperature\":\"31\",\"lat\":12.5092044,\"lng\":-70.0086306}", CityTemperature.class);
  }

  @Test
  public void setCity_resultOk() throws Exception {
    CityTemperature cityTemp = new CityTemperature();
    cityTemp.setCity("Paris");
    assertEquals(cityTemp.getCity(), "Paris");
  }

  @Test
  public void setTemperature_resultOkPositive() throws Exception {
    CityTemperature cityTemp = new CityTemperature();
    cityTemp.setTemperature(35.8);
    assertTrue(cityTemp.getTemperature() == 35.8);
  }

  @Test
  public void setTemperature_resultOkNegative() throws Exception {
    CityTemperature cityTemp = new CityTemperature();
    cityTemp.setTemperature(-35.8);
    assertTrue(cityTemp.getTemperature() == -35.8);
  }

  @Test
  public void setLat_resultOkPositive() throws Exception {
    CityTemperature cityTemp = new CityTemperature();
    cityTemp.setLat(41.3275459);
    assertTrue(cityTemp.getLat() == 41.3275459);
  }

  @Test
  public void setLat_resultOkNegative() throws Exception {
    CityTemperature cityTemp = new CityTemperature();
    cityTemp.setLat(-41.3275459);
    assertTrue(cityTemp.getLat() == -41.3275459);
  }

  @Test
  public void setLng_resultOkPositive() throws Exception {
    CityTemperature cityTemp = new CityTemperature();
    cityTemp.setLng(19.81869819999997);
    assertTrue(cityTemp.getLng() == 19.81869819999997);
  }

  @Test
  public void setLng_resultOkNegative() throws Exception {
    CityTemperature cityTemp = new CityTemperature();
    cityTemp.setLng(-70.0086306);
    assertTrue(cityTemp.getLng() == -70.0086306);
  }

    /** Test that {@link CityTemperature.equals} returns False when applied to null. */
  @Test
  public void equals_null() throws Exception {
    assertFalse(cityTirana1.equals(null));
  }

  /**
   * Test that {@link CityTemperature.equals} returns False when applied to an object of different
   * class than CityTemperature.
   */
  @Test
  public void equals_differentClass() throws Exception {
    assertFalse(cityTirana1.equals(""));
  }

  /** Test that {@link CityTemperature.equals} returns True when applied to self. */
  @Test
  public void equals_self() throws Exception {
    assertTrue(cityTirana1.equals(cityTirana1));
  }

  /**
   * Test that {@link CityTemperature.equals} returns True when applied to an object of class
   * CityTemperature with identical attribute values.
   */
  @Test
  public void equals_identical() throws Exception {
    assertTrue(cityTirana1.equals(cityTirana2));
  }

  /**
   * Test that {@link CityTemperature.equals} returns False when applied to an object of class
   * CityTemperature with different attribute values.
   */
  @Test
  public void equals_different() throws Exception {
    assertFalse(cityTirana1.equals(cityOranjestad));
  }

  /**
   * Test that {@link CityTemperature.equals} returns True when comparing two CityTemperature
   * objects with null name and other attributes identical.
   */
  @Test
  public void equals_betweenNoName() throws Exception {
    assertFalse(cityNoName1.equals(cityNoName2));
  }

  /**
   * Test that {@link CityTemperature.equals} returns False when applied to an object of class
   * CityTemperature with null name.
   */
  @Test
  public void equals_withNoName() throws Exception {
    assertFalse(cityNoName1.equals(cityTirana2));
  }
}
