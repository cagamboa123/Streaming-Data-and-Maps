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
import com.google.datastore.v1.Entity;
import com.google.datastore.v1.Key;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformation converting a message from JSON to Entity format for writing into BigQuery
 */
public class JsonToEntity extends DoFn<KV<Message, CityTemperature>, Entity> {

  private static final long serialVersionUID = 1462827258689031685L;

  private static final Logger LOG = LoggerFactory.getLogger(JsonToEntity.class);

  @ProcessElement
  public void processElement(ProcessContext c)  {
    // Read the next message to process
    KV<Message, CityTemperature> temperatureTuple = c.element();
    Message message = temperatureTuple.getKey();

    // Create ancestor key
    Key cityKey = makeKey("City", message.getId()).build();
    Key cityTemperatureKey =
        makeKey(cityKey, "CityTemperature", String.valueOf(message.getTimestamp())).build();

    // Create entity and write it to the output PCollection
    Entity.Builder entityBuilder = Entity.newBuilder();
    entityBuilder.setKey(cityTemperatureKey);
    entityBuilder.getMutableProperties()
        .put("temperature", makeValue(message.getTemperature()).build());
    Entity cityTemperature = entityBuilder.build();
    c.output(cityTemperature);
  }
}
