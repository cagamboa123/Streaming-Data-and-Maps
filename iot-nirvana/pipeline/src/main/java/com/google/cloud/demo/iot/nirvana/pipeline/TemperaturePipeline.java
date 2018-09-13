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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.datastore.DatastoreIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;

/**
 * Pipeline reading the temperature data coming from the IoT devices through IoT Core and writing
 * them to BigQuery and Datastore
 */
public class TemperaturePipeline {

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss");

  public static void main(String[] args) {

    try {
      // Create the pipeline with options
      TemperaturePipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
          .as(TemperaturePipelineOptions.class);
      Pipeline p = Pipeline.create(options);

      // Read messages from Pub/Sub
      PCollection<String> pubSubMessages =
          p.apply(
              "PubSubRead",
              PubsubIO.readStrings().fromTopic(options.getInputTopic()));

      // Filter out messages that don't contain temperatures for actual cities
      PCollectionView<Map<String, CityTemperature>> cities = loadCitiesSideInput(p);
      TupleTag<KV<Message, CityTemperature>> goodTag =
          new TupleTag<KV<Message, CityTemperature>>() {};
      TupleTag<String> errorTag = new TupleTag<String>() {};
      PCollectionTuple filteredMessages = pubSubMessages
          .apply(
            "FilterMessages",
            ParDo
              .of(FilterMessages.newBuilder().setCities(cities).setErrorTag(errorTag).build())
              .withSideInputs(cities)
              .withOutputTags(goodTag, TupleTagList.of(errorTag)));
      PCollection<KV<Message, CityTemperature>> validTemperatures = filteredMessages.get(goodTag);

      // Convert messages into TableRow and write them to BigQuery
      validTemperatures
          .apply("JsonToTableRow", ParDo.of(new JsonToTableRow()))
          .apply(
            "BigQueryWrite",
            BigQueryIO
              .writeTableRows()
              .to(options.getTable())
              .withSchema(JsonToTableRow.getSchema())
              .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
              .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

      // Convert messages to Entity objects and write them to Datastore
      validTemperatures
          .apply(
            "JsonToEntity", ParDo.of(new JsonToEntity()))
          .apply(
            "DatastoreWrite",
            DatastoreIO.v1().write().withProjectId(options.as(GcpOptions.class).getProject()));

      // Execute pipeline
      p.run();
    } catch (NoSuchAlgorithmException e) {
      //TODO: add error logging here
    } catch (UnsupportedEncodingException e) {
      //TODO: add error logging here
    }
  }

  /**
   * Load the list of cities to be used as a side input
   */
  static PCollectionView<Map<String, CityTemperature>> loadCitiesSideInput(Pipeline p)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    final List<KV<String, CityTemperature>> cities = TemperatureUtils.loadCities();
    PCollection<KV<String, CityTemperature>> citiesCollection =
        p.apply("LoadCities", Create.of(cities));
    PCollectionView<Map<String, CityTemperature>> citiesCollectionView =
        citiesCollection.apply("CitiesToSingleton", View.<String, CityTemperature>asMap());
    return citiesCollectionView;
  }
}
