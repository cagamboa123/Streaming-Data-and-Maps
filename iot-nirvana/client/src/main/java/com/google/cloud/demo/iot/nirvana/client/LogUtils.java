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

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Payload.StringPayload;
import com.google.cloud.logging.Severity;
import java.util.Collections;

/** Class that describes shared utils functions that will be used throughout the application */
public final class LogUtils {

  public static final String CLOUD_LOGGING_DEVICE_SIMULATOR = "DeviceSimulator";
  public static final String CLOUD_LOGGING_MONITORED_RESOURCE_NAME = "Cloud-IoT-Core";

  /**
   * Write logs into Cloud Logging
   *
   * @param logging the object used to write logs into Cloud Logging
   * @param severity the severity of the log INFO, DEBUG, WARNING, ERROR
   * @param text the message that you want to log
   * @param logName the name of your log
   * @param gcpProjectId the name of the project where you want to write logs
   */
  public static void writeLog(Logging logging, String text, Severity severity,
      String monitoredResourceName, String logName, String gcpProjectId) {
    // prepare entry
    LogEntry entry = LogEntry.newBuilder(StringPayload.of(text))
        .setSeverity(severity)
        .setLogName(logName)
        .setResource(
          MonitoredResource.newBuilder("logging_log")
            .addLabel("project_id", gcpProjectId)
            .addLabel("name", monitoredResourceName)
            .build())
        .build();

    // Writes the log entry asynchronously
    logging.write(Collections.singleton(entry));
  }
}
