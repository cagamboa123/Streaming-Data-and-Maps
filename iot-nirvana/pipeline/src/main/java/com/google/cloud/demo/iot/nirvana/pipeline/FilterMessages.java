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

import com.google.auto.value.AutoValue;
import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import com.google.cloud.demo.iot.nirvana.common.Message;
import com.google.gson.Gson;
import java.util.Map;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;

/**
 * Transformation filtering out temperatuire messages that don't contain data for actual cities.
 */
@AutoValue
public abstract class FilterMessages extends DoFn<String, KV<Message, CityTemperature>> {

  public abstract TupleTag<String> getErrorTag();
  public abstract PCollectionView<Map<String, CityTemperature>> getCities();

  /** Builder for FilterMessages. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setCities(PCollectionView<Map<String, CityTemperature>> cities);
    public abstract Builder setErrorTag(TupleTag<String> errorTag);
    public abstract FilterMessages build();
  }

  public static Builder newBuilder() {
    return new AutoValue_FilterMessages.Builder();
  }

  @ProcessElement
  public void processElement(ProcessContext c) {
    // Read the next message to process
    String strMessage = c.element();
    Gson gson = new Gson();
    Message message = gson.fromJson(strMessage, Message.class);

    // Filter out messages that don't contain temperatures for actual cities
    Map<String, CityTemperature> citiesMap = c.sideInput(getCities());
    if (citiesMap.containsKey(message.getId())) {
      CityTemperature city = citiesMap.get(message.getId());
      c.output(KV.of(message, city));
    } else {
      c.output(getErrorTag(), strMessage);
    }
  }

}
