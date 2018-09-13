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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Message}. */
@RunWith(JUnit4.class)
public final class MessageTest {

  public void constructor_resultOk() throws Exception {
    Message message = new Message("8762723AE028CAA144CBF8B7069003C3", 31, 1523999305000L);
    assertEquals(message.getId(), "8762723AE028CAA144CBF8B7069003C3");
    assertTrue(message.getTemperature() == 31);
    assertTrue(message.getTimestamp() == 1523999305000L);
  }

  @Test
  public void setId_resultDirectOk() throws Exception {
    Message message = new Message();
    message.setId("8762723AE028CAA144CBF8B7069003C3");
    assertEquals(message.getId(), "8762723AE028CAA144CBF8B7069003C3");
  }

  @Test
  public void setId_resultLatLongOk() throws Exception {
    Message message = new Message();
    message.setId(41.3275459, 19.81869819999997);
    assertEquals(message.getId(), "8762723AE028CAA144CBF8B7069003C3");
  }

  @Test
  public void setTemperature_resultOkPositive() throws Exception {
    Message message = new Message();
    message.setTemperature(31);
    assertTrue(message.getTemperature() == 31);
  }

  @Test
  public void setTemperature_resultOkNegative() throws Exception {
    Message message = new Message();
    message.setTemperature(-31);
    assertTrue(message.getTemperature() == -31);
  }

  @Test
  public void setTimestamp_resultOkPositive() throws Exception {
    Message message = new Message();
    message.setTimestamp(1523999305000L);
    assertTrue(message.getTimestamp() == 1523999305000L);
  }
}
