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

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import com.google.cloud.demo.iot.nirvana.common.Message;
import com.google.cloud.demo.iot.nirvana.common.TemperatureUtils;
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

/** Unit tests for {@link JsonToTableRow}. */
@RunWith(JUnit4.class)
public final class JsonToTableRowTest {

  @Rule public final transient TestPipeline pipeline = TestPipeline.create();

  private Message messageTirana;
  private Message messageOranjestad;
  private CityTemperature cityTirana;
  private CityTemperature cityOranjestad;
  private TableRow rowTirana;
  private TableRow rowOranjestad;

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

    String idOranjestad = "12.5092044--70.0086306";
    String keyOranjestad = TemperatureUtils.toHexString(md.digest(idOranjestad.getBytes("UTF-8")));
    String messageOranjestadStr = String.format(
        "{\"id\":\"%s\",\"temperature\":\"31\",\"timestamp\":1523999305000}", keyOranjestad);
    messageOranjestad = gson.fromJson(messageOranjestadStr, Message.class);
    cityOranjestad = gson.fromJson(
        "{\"city\":\"Aruba-Oranjestad\",\"temperature\":\"31\",\"lat\":12.5092044,\"lng\":-70.0086306}",
        CityTemperature.class);

    rowTirana = new TableRow()
          .set("Id", keyTirana)
          .set("City", "Albania-Tirana")
          .set("Lat", "41.3275459")
          .set("Lng", "19.81869819999997")
          .set("Temperature", String.valueOf((double) 18))
          .set("Time", dataFormat.format(1523994520000L))
          .set("Year", "2018")
          .set("Month", "04")
          .set("Day", "17")
          .set("Hour", "21")
          .set("Minute", "48")
          .set("Second", "40")
          .set("Frame", "000");

    rowOranjestad = new TableRow()
          .set("Id", keyOranjestad)
          .set("City", "Aruba-Oranjestad")
          .set("Lat", "12.5092044")
          .set("Lng", "-70.0086306")
          .set("Temperature", String.valueOf((double) 31))
          .set("Time", dataFormat.format(1523999305000L))
          .set("Year", "2018")
          .set("Month", "04")
          .set("Day", "17")
          .set("Hour", "23")
          .set("Minute", "08")
          .set("Second", "25")
          .set("Frame", "000");
  }

  /** Test {@link JsonToTableRow.processElement} with a single temperature message. */
  @Test
  @Category(NeedsRunner.class)
  public void testProcess_singleTemperature() throws Exception {
    // Create test data
    List<KV<Message, CityTemperature>> inputMessages = Arrays.asList(
        KV.of(messageTirana, cityTirana));
    List<TableRow> expectedResult = Arrays.asList(rowTirana);

    // Run the test
    PCollection<TableRow> results = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply("JsonToTableRow", ParDo.of(new JsonToTableRow()));

    // Check the results
    PAssert.that(results).containsInAnyOrder(expectedResult);
    pipeline.run();
  }

  /** Test {@link JsonToTableRow.processElement} with multiple temperature messages. */
  @Test
  @Category(NeedsRunner.class)
  public void testProcess_multipleTemperatures() throws Exception {
    // Create test data
    List<KV<Message, CityTemperature>> inputMessages = Arrays.asList(
        KV.of(messageTirana, cityTirana), KV.of(messageOranjestad, cityOranjestad));
    List<TableRow> expectedResult = Arrays.asList(rowTirana, rowOranjestad);

    // Run the test
    PCollection<TableRow> results = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply("JsonToTableRow", ParDo.of(new JsonToTableRow()));

    // Check the results
    PAssert.that(results).containsInAnyOrder(expectedResult);
    pipeline.run();
  }
}
