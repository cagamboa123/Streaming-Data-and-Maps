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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Message}. */
@RunWith(JUnit4.class)
public final class MessageTest {

  private Message messageTirana1;
  private Message messageTirana2;
  private Message messageOranjestad;
  private Message messageNoId1;
  private Message messageNoId2;

  /** Create test data. */
  @Before
  public void createTestData() throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    Gson gson = new Gson();
    String idTirana = "41.3275459-19.81869819999997";
    String keyTirana = TemperatureUtils.toHexString(md.digest(idTirana.getBytes("UTF-8")));
    String messageTiranaStr = String.format(
        "{\"id\":\"%s\",\"temperature\":\"18\",\"timestamp\":1523994520000}", keyTirana);
    messageTirana1 = gson.fromJson(messageTiranaStr, Message.class);
    messageTirana2 = gson.fromJson(messageTiranaStr, Message.class);
    String idOranjestad = "12.5092044--70.0086306";
    String keyOranjestad = TemperatureUtils.toHexString(md.digest(idOranjestad.getBytes("UTF-8")));
    String messageOranjestadStr = String.format(
        "{\"id\":\"%s\",\"temperature\":\"31\",\"timestamp\":1523999305000}", keyOranjestad);
    messageOranjestad = gson.fromJson(messageOranjestadStr, Message.class);
    messageNoId1 = gson.fromJson(
        "{\"temperature\":\"31\",\"timestamp\":1523999305000}", Message.class);
    messageNoId2 = gson.fromJson(
        "{\"temperature\":\"31\",\"timestamp\":1523999305000}", Message.class);
  }

  /** Test that {@link Message.equals} returns False when applied to null. */
  @Test
  public void equals_null() throws Exception {
    assertFalse(messageTirana1.equals(null));
  }

  /**
   * Test that {@link Message.equals} returns False when applied to an object of different class
   * than Message.
   */
  @Test
  public void equals_differentClass() throws Exception {
    assertFalse(messageTirana1.equals(""));
  }

  /** Test that {@link Message.equals} returns True when applied to self. */
  @Test
  public void equals_self() throws Exception {
    assertTrue(messageTirana1.equals(messageTirana1));
  }

  /**
   * Test that {@link Message.equals} returns True when applied to an object of class Message with
   * identical attribute values.
   */
  @Test
  public void equals_identical() throws Exception {
    assertTrue(messageTirana1.equals(messageTirana2));
  }

  /**
   * Test that {@link Message.equals} returns False when applied to an object of class Message with
   * different attribute values.
   */
  @Test
  public void equals_different() throws Exception {
    assertFalse(messageTirana1.equals(messageOranjestad));
  }

  /**
   * Test that {@link Message.equals} returns True when comparing two Message objects with null id
   * and other attributes identical.
   */
  @Test
  public void equals_betweenNoId() throws Exception {
    assertFalse(messageNoId1.equals(messageNoId2));
  }

  /**
   * Test that {@link Message.equals} returns False when applied to an object of class Message with
   * null name.
   */
  @Test
  public void equals_withNoId() throws Exception {
    assertFalse(messageNoId1.equals(messageTirana1));
  }
}
