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

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CityTemperature}. */
@RunWith(JUnit4.class)
public final class TemperatureUtilsTest {

  @Test
  public void loadCityTemperature_resultOk() throws Exception {
    Gson gson = new Gson();
    CityTemperature cityTirana1 = gson.fromJson(
        "{\"city\":\"Albania-Tirana\",\"temperature\":\"18\",\"lat\":41.3275459,\"lng\":19.81869819999997}",
        CityTemperature.class);
    CityTemperature cityTirana2 = TemperatureUtils.loadCityTemperature(0);
    assertEquals(cityTirana2, cityTirana1);
  }
}
