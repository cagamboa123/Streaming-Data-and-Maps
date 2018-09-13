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

package com.google.cloud.demo.iot.nirvana.pipeline;

import static com.google.datastore.v1.client.DatastoreHelper.makeKey;
import static com.google.datastore.v1.client.DatastoreHelper.makeValue;

import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import com.google.cloud.demo.iot.nirvana.common.Message;
import com.google.cloud.demo.iot.nirvana.common.TemperatureUtils;
import com.google.datastore.v1.Entity;
import com.google.datastore.v1.Key;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link JsonToEntity}. */
@RunWith(JUnit4.class)
public final class JsonToEntityTest {

  @Rule public final transient TestPipeline pipeline = TestPipeline.create();

  private Message messageTirana;
  private Message messageOranjestad;
  private CityTemperature cityTirana;
  private CityTemperature cityOranjestad;
  private Entity entityTirana;
  private Entity entityOranjestad;

  /** Create test data. */
  @Before
  public void createTestData() throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    Gson gson = new Gson();
    SimpleDateFormat dataFormat = JsonToTableRow.createDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS");

    String idTirana = "41.3275459-19.81869819999997";
    String keyTirana = TemperatureUtils.toHexString(md.digest(idTirana.getBytes("UTF-8")));
    String messageTiranaStr = String.format(
        "{\"id\":\"%s\",\"temperature\":\"18\",\"timestamp\":1523994520000}", keyTirana);
    messageTirana = gson.fromJson(messageTiranaStr, Message.class);
    cityTirana = gson.fromJson(
        "{\"city\":\"Albania-Tirana\",\"temperature\":\"18\",\"lat\":41.3275459,\"lng\":19.81869819999997}",
        CityTemperature.class);

    String timestampTirana = String.valueOf(messageTirana.getTimestamp());
    Key entityKeyTirana =
        makeKey(makeKey("City", keyTirana).build(), "CityTemperature", timestampTirana)
        .build();
    Entity.Builder entityBuilder = Entity.newBuilder();
    entityBuilder.setKey(entityKeyTirana);
    entityBuilder
        .getMutableProperties()
        .put("temperature", makeValue(messageTirana.getTemperature()).build());
    entityTirana = entityBuilder.build();

    String idOranjestad = "12.5092044--70.0086306";
    String keyOranjestad =
        TemperatureUtils.toHexString(md.digest(idOranjestad.getBytes("UTF-8")));
    String messageOranjestadStr = String.format(
        "{\"id\":\"%s\",\"temperature\":\"31\",\"timestamp\":1523999305000}", keyOranjestad);
    messageOranjestad = gson.fromJson(messageOranjestadStr, Message.class);
    cityOranjestad = gson.fromJson(
        "{\"city\":\"Aruba-Oranjestad\",\"temperature\":\"31\",\"lat\":12.5092044,\"lng\":-70.0086306}",
        CityTemperature.class);

    String timestampOranjestad = String.valueOf(messageOranjestad.getTimestamp());
    Key entityKeyOranjestad =
        makeKey(makeKey("City", keyOranjestad).build(), "CityTemperature", timestampOranjestad)
        .build();
    entityBuilder = Entity.newBuilder();
    entityBuilder.setKey(entityKeyOranjestad);
    entityBuilder
        .getMutableProperties()
        .put("temperature", makeValue(messageOranjestad.getTemperature()).build());
    entityOranjestad = entityBuilder.build();
  }

  /** Test {@link JsonToEntity.processElement} with a single temperature message. */
  @Test
  @Category(NeedsRunner.class)
  public void testProcess_singleTemperature() throws Exception {
    // Create test data
    List<KV<Message, CityTemperature>> inputMessages = Arrays.asList(
        KV.of(messageTirana, cityTirana));
    List<Entity> expectedResult = Arrays.asList(entityTirana);

    // Run the test
    PCollection<Entity> results = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply("JsonToEntity", ParDo.of(new JsonToEntity()));

    // Check the results
    PAssert.that(results).containsInAnyOrder(expectedResult);
    pipeline.run();
  }

  /** Test {@link JsonToEntity.processElement} with multiple temperature messages. */
  @Test
  @Category(NeedsRunner.class)
  public void testProcess_multipleTemperatures() throws Exception {
    // Create test data
    List<KV<Message, CityTemperature>> inputMessages = Arrays.asList(
        KV.of(messageTirana, cityTirana), KV.of(messageOranjestad, cityOranjestad));
    List<Entity> expectedResult = Arrays.asList(entityTirana, entityOranjestad);

    // Run the test
    PCollection<Entity> results = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply("JsonToEntity", ParDo.of(new JsonToEntity()));

    // Check the results
    PAssert.that(results).containsInAnyOrder(expectedResult);
    pipeline.run();
  }
}
