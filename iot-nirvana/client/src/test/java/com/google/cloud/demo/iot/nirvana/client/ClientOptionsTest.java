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

package com.google.cloud.demo.iot.nirvana.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ClientOptions}. */
@RunWith(JUnit4.class)
public final class ClientOptionsTest {

  private String argsIncomplete[] = {
    "-region", "us-central1-f",
    "-pubSubTopicName", "projects/my-project/topics/my-topic",
    "-registryName", "my-registry",
    "-rsaCertificateFilePath", "/tmp/iot-core/rsa_cert_0.pem",
    "-privateKey", "/tmp/iot-core/rsa_private_0_pkcs8",
    "-cityIndex", "1"};

  private String argsComplete[] = {
    "-projectId", "my-project",
    "-region", "us-central1-f",
    "-pubSubTopicName", "projects/my-project/topics/my-topic",
    "-registryName", "my-registry",
    "-rsaCertificateFilePath", "/tmp/iot-core/rsa_cert_0.pem",
    "-privateKey", "/tmp/iot-core/rsa_private_0_pkcs8",
    "-cityIndex", "1"};

  @Test
  public void parse_exceptionThrownArgsEmpty() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    String args[] = new String[0];
    boolean isThrown = false;
    try {
      options.parse(args);
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void parse_exceptionThrownArgsIncomplete() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.parse(argsIncomplete);
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void parse_resultOkArgsComplete() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.parse(argsComplete);
    } catch (ClientException e) {
      isThrown = true;
    }
    assertFalse(isThrown);
  }

  @Test
  public void getGcpProjectId_resultOk() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    assertEquals(options.getGcpProjectId(), "my-project");
  }

  @Test
  public void getGcpProjectId_exceptionThrown() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.getGcpProjectId();
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void getGcpRegion_resultOk() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    assertEquals(options.getGcpRegion(), "us-central1-f");
  }

  @Test
  public void getGcpRegion_exceptionThrown() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.getGcpRegion();
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void getRegistryName_resultOk() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    assertEquals(options.getRegistryName(), "my-registry");
  }

  @Test
  public void getRegistryName_exceptionThrown() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.getRegistryName();
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void getPubSubTopicName_resultOk() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    assertEquals(options.getPubSubTopicName(), "projects/my-project/topics/my-topic");
  }

  @Test
  public void getPubSubTopicName_exceptionThrown() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.getPubSubTopicName();
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void getRsaCertificateFilePath_resultOk() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    assertEquals(options.getRsaCertificateFilePath(), "/tmp/iot-core/rsa_cert_0.pem");
  }

  @Test
  public void getRsaCertificateFilePath_exceptionThrown() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.getRsaCertificateFilePath();
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void getPrivateKey_resultOk() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    assertEquals(options.getPrivateKey(), "/tmp/iot-core/rsa_private_0_pkcs8");
  }

  @Test
  public void getPrivateKey_exceptionThrown() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.getPrivateKey();
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }

  @Test
  public void getCityIndex_resultOk() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    assertEquals(options.getCityIndex(), "1");
  }

  @Test
  public void getCityIndex_exceptionThrown() throws Exception {
    ClientOptions options = ClientOptions.newBuilder().build();
    boolean isThrown = false;
    try {
      options.getCityIndex();
    } catch (ClientException e) {
      isThrown = true;
    }
    assertTrue(isThrown);
  }
}
