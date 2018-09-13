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

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import com.google.cloud.demo.iot.nirvana.common.Message;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformation converting a message from JSON to TableRow format for writing into BigQuery
 */
public class JsonToTableRow extends DoFn<KV<Message, CityTemperature>, TableRow> {

  private static final long serialVersionUID = 1462827258689031685L;

  private static final Logger LOG = LoggerFactory.getLogger(JsonToTableRow.class);

  private static final TimeZone PARIS_TIME_ZONE = TimeZone.getTimeZone("Europe/Paris");
  private static final SimpleDateFormat DF = createDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS");
  private static final SimpleDateFormat DF_YEAR = createDateFormat("YYYY");
  private static final SimpleDateFormat DF_MONTH = createDateFormat("MM");
  private static final SimpleDateFormat DF_DAY = createDateFormat("dd");
  private static final SimpleDateFormat DF_HOUR = createDateFormat("HH");
  private static final SimpleDateFormat DF_MINUTE = createDateFormat("mm");
  private static final SimpleDateFormat DF_SECOND = createDateFormat("ss");
  private static final SimpleDateFormat DF_FRAME = createDateFormat("SSS");

  @ProcessElement
  public void processElement(ProcessContext c) {
    // Read the next message to process
    KV<Message, CityTemperature> temperatureTuple = c.element();
    Message message = temperatureTuple.getKey();
    CityTemperature city = temperatureTuple.getValue();

    // Create a TableRow from the temperature Message
    TableRow row = new TableRow()
        .set("Id", message.getId())
        .set("City", city.getCity())
        .set("Lat", String.valueOf(city.getLat()))
        .set("Lng", String.valueOf(city.getLng()))
        .set("Temperature", String.valueOf(message.getTemperature()))
        .set("Time", DF.format(message.getTimestamp()))
        .set("Year", DF_YEAR.format(message.getTimestamp()))
        .set("Month", DF_MONTH.format(message.getTimestamp()))
        .set("Day", DF_DAY.format(message.getTimestamp()))
        .set("Hour", DF_HOUR.format(message.getTimestamp()))
        .set("Minute", DF_MINUTE.format(message.getTimestamp()))
        .set("Second", DF_SECOND.format(message.getTimestamp()))
        .set("Frame", DF_FRAME.format(message.getTimestamp()));
    c.output(row);
  }

  /**
   * Create the schema of the BigQuery table storing the temperatures
   */
  public static TableSchema getSchema() {
    return new TableSchema()
        .setFields(
            new ArrayList<TableFieldSchema>() {
              // Compose the list of TableFieldSchema from tableSchema.
              // TODO: fields to be reviewed
              {
                add(new TableFieldSchema().setName("ID").setType("STRING"));
                add(new TableFieldSchema().setName("City").setType("STRING"));
                add(new TableFieldSchema().setName("Lat").setType("STRING"));
                add(new TableFieldSchema().setName("Lng").setType("STRING"));
                add(new TableFieldSchema().setName("Temperature").setType("STRING"));
                add(new TableFieldSchema().setName("Time").setType("DATETIME"));
                add(new TableFieldSchema().setName("Year").setType("INTEGER"));
                add(new TableFieldSchema().setName("Month").setType("INTEGER"));
                add(new TableFieldSchema().setName("Day").setType("INTEGER"));
                add(new TableFieldSchema().setName("Hour").setType("INTEGER"));
                add(new TableFieldSchema().setName("Minute").setType("INTEGER"));
                add(new TableFieldSchema().setName("Second").setType("INTEGER"));
                add(new TableFieldSchema().setName("Frame").setType("INTEGER"));
              }
            });
  }

  /** Creates a date format for a specific pattern with the correct time zone. */
  static SimpleDateFormat createDateFormat(String pattern) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
    dateFormat.setTimeZone(PARIS_TIME_ZONE);
    return dateFormat;
  }
}
