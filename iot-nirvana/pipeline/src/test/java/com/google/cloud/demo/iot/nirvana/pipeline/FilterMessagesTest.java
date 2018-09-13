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

import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import com.google.cloud.demo.iot.nirvana.common.Message;
import com.google.cloud.demo.iot.nirvana.common.TemperatureUtils;
import com.google.gson.Gson;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link FilterMessages}. */
@RunWith(JUnit4.class)
public final class FilterMessagesTest implements Serializable {

  @Rule public final transient TestPipeline pipeline = TestPipeline.create();

  private String invalidMessage;
  private String validMessageTiranaStr;
  private String validMessageOranjestadStr;
  private Message validMessageTirana;
  private Message validMessageOranjestad;
  private CityTemperature cityTirana;
  private CityTemperature cityOranjestad;
  private PCollectionView<Map<String, CityTemperature>> cities;
  private TupleTag<KV<Message, CityTemperature>> goodTag;
  private TupleTag<String> errorTag;

  /** Create test data. */
  @Before
  public void createTestData() throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    Gson gson = new Gson();

    invalidMessage =
        "{\"id\":\"invalid id\",\"temperature\":\"18\",\"timestamp\":1523994520000}";

    String idTirana = "41.3275459-19.81869819999997";
    String keyTirana = TemperatureUtils.toHexString(md.digest(idTirana.getBytes("UTF-8")));
    validMessageTiranaStr = String.format(
        "{\"id\":\"%s\",\"temperature\":\"18\",\"timestamp\":1523994520000}", keyTirana);
    validMessageTirana = gson.fromJson(validMessageTiranaStr, Message.class);
    cityTirana = gson.fromJson(
        "{\"city\":\"Albania-Tirana\",\"temperature\":\"18\",\"lat\":41.3275459,\"lng\":19.81869819999997}",
        CityTemperature.class);

    String idOranjestad = "12.5092044--70.0086306";
    String keyOranjestad = TemperatureUtils.toHexString(md.digest(idOranjestad.getBytes("UTF-8")));
    validMessageOranjestadStr = String.format(
        "{\"id\":\"%s\",\"temperature\":\"31\",\"timestamp\":1523999305000}", keyOranjestad);
    validMessageOranjestad = gson.fromJson(validMessageOranjestadStr, Message.class);
    cityOranjestad = gson.fromJson(
        "{\"city\":\"Aruba-Oranjestad\",\"temperature\":\"31\",\"lat\":12.5092044,\"lng\":-70.0086306}",
        CityTemperature.class);

    cities = TemperaturePipeline.loadCitiesSideInput(pipeline);
    goodTag = new TupleTag<KV<Message, CityTemperature>>() {};
    errorTag = new TupleTag<String>() {};
  }

  /**
   * Test {@link FilterMessages.processElement} with an invalid message, whose identifier does not
   * correspond to the coordinates of an actual city.
   */
  @Test
  @Category(NeedsRunner.class)
  public void methodUnderTest_expectedResult() throws Exception {
    // Create test data
    List<String> inputMessages = Arrays.asList(invalidMessage);

    // Create pipeline graph
    PCollectionTuple filteredMessages = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply(
          "FilterMessages",
          ParDo
            .of(FilterMessages.newBuilder().setCities(cities).setErrorTag(errorTag).build())
            .withSideInputs(cities)
            .withOutputTags(goodTag, TupleTagList.of(errorTag)));
    PCollection<KV<Message, CityTemperature>> validTemperatures = filteredMessages.get(goodTag);
    PCollection<String> invalidTemperatures = filteredMessages.get(errorTag);

    // Run pipeline and evaluate results
    PAssert.that(validTemperatures).empty();
    PAssert.that(invalidTemperatures).containsInAnyOrder(inputMessages);
    pipeline.run();
  }

  /**
   * Test {@link FilterMessages.processElement} with an valid message, whose identifier corresponds
   * to the coordinates of an actual city.
   */
  @Test
  @Category(NeedsRunner.class)
  public void testProcessElement_singleValidMessage() throws Exception {
    // Create test data
    List<String> inputMessages = Arrays.asList(validMessageTiranaStr);
    List<KV<Message, CityTemperature>> expectedResult = Arrays.asList(
        KV.of(validMessageTirana, cityTirana));

    // Create pipeline graph
    PCollectionTuple filteredMessages = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply(
          "FilterMessages",
          ParDo
            .of(FilterMessages.newBuilder().setCities(cities).setErrorTag(errorTag).build())
            .withSideInputs(cities)
            .withOutputTags(goodTag, TupleTagList.of(errorTag)));
    PCollection<KV<Message, CityTemperature>> validTemperatures = filteredMessages.get(goodTag);
    PCollection<String> invalidTemperatures = filteredMessages.get(errorTag);

    // Run pipeline and evaluate results
    PAssert.that(validTemperatures).containsInAnyOrder(expectedResult);
    PAssert.that(invalidTemperatures).empty();
    pipeline.run();
  }

  /**
   * Test {@link FilterMessages.processElement} with multiple valid messages, whose respective
   * identifiers corresponds to coordinates of an actual cities.
   */
  @Test
  @Category(NeedsRunner.class)
  public void testProcessElement_multipleValidMessages() throws Exception {
    // Create test data
    List<String> inputMessages = Arrays.asList(validMessageTiranaStr, validMessageOranjestadStr);
    List<KV<Message, CityTemperature>> expectedResult = Arrays.asList(
        KV.of(validMessageTirana, cityTirana), KV.of(validMessageOranjestad, cityOranjestad));

    // Create pipeline graph
    PCollectionTuple filteredMessages = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply(
          "FilterMessages",
          ParDo
            .of(FilterMessages.newBuilder().setCities(cities).setErrorTag(errorTag).build())
            .withSideInputs(cities)
            .withOutputTags(goodTag, TupleTagList.of(errorTag)));
    PCollection<KV<Message, CityTemperature>> validTemperatures = filteredMessages.get(goodTag);
    PCollection<String> invalidTemperatures = filteredMessages.get(errorTag);

    // Run pipeline and evaluate results
    PAssert.that(validTemperatures).containsInAnyOrder(expectedResult);
    PAssert.that(invalidTemperatures).empty();
    pipeline.run();
  }

  /**
   * Test {@link FilterMessages.processElement} with a mix of valid and invalid messages, whose
   * respective identifiers corresponds to coordinates of an actual cities.
   */
  @Test
  @Category(NeedsRunner.class)
  public void testProcessElement_invalidMessage() throws Exception {
        // Create test data
    List<String> inputMessages = Arrays.asList(
        invalidMessage, validMessageTiranaStr, validMessageOranjestadStr);
    List<KV<Message, CityTemperature>> expectedValidResult = Arrays.asList(
        KV.of(validMessageTirana, cityTirana), KV.of(validMessageOranjestad, cityOranjestad));

    // Create pipeline graph
    PCollectionTuple filteredMessages = pipeline
        .apply("Create", Create.of(inputMessages))
        .apply(
          "FilterMessages",
          ParDo
            .of(FilterMessages.newBuilder().setCities(cities).setErrorTag(errorTag).build())
            .withSideInputs(cities)
            .withOutputTags(goodTag, TupleTagList.of(errorTag)));
    PCollection<KV<Message, CityTemperature>> validTemperatures = filteredMessages.get(goodTag);
    PCollection<String> invalidTemperatures = filteredMessages.get(errorTag);

    // Run pipeline and evaluate results
    PAssert.that(validTemperatures).containsInAnyOrder(expectedValidResult);
    PAssert.that(invalidTemperatures).containsInAnyOrder(invalidMessage);
    pipeline.run();
  }
}
