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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Charsets;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.api.services.cloudiot.v1.model.PublicKeyCredential;
import com.google.auto.value.AutoValue;
import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import com.google.cloud.demo.iot.nirvana.common.TemperatureException;
import com.google.cloud.demo.iot.nirvana.common.TemperatureUtils;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Severity;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/** An IoT device sending temperature data to a device registty on Google Cloud IoT Core */
@AutoValue
public abstract class IotDevice {

  static final Logging logging = LoggingOptions.getDefaultInstance().getService();

  public static final String APP_NAME_API = "iot-device-simulator";

  public static final String KEY_FORMAT_RSA_X509_PEM = "RSA_X509_PEM";

  public abstract ClientOptions getOptions();

  private String deviceId;

  /** Builder for IotClient. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setOptions(ClientOptions options);

    public abstract IotDevice build();
  }

  public static Builder newBuilder() {
    return new AutoValue_IotDevice.Builder();
  }

  /** Register the device. */
  public void register() throws ClientException {
    try {
      logDebug("Registering Cloud IoT device...");
      generateDeviceId();
      DeviceCredential credentials = loadCredentials();
      Device device = new Device();
      device.setId(deviceId);
      device.setCredentials(Arrays.asList(credentials));
      String registryPath = generateRegistryPath();
      CloudIot service = createCloudIotService();
      Device registeredDevice =
          service
              .projects()
              .locations()
              .registries()
              .devices()
              .create(registryPath, device)
              .execute();
      logDebug("Cloud IoT device registered.\n" + registeredDevice.toPrettyString());
    } catch (IOException e) {
      String message = String.format("Failed to register device. Cause: {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    }
  }

  /** Publish messages continuously to Cloud IoT Core. */
  public void publish() throws ClientException, TemperatureException {
    // Get the city temperature for the configured city
    CityTemperature cityTemperature = TemperatureUtils.loadCityTemperature(
        Integer.parseInt(getOptions().getCityIndex()));

    // Create the publisher and start publishing
    MessagePublisher publisher =
        MessagePublisher.newBuilder()
            .setOptions(getOptions())
            .setDeviceId(deviceId)
            .setCityTemperature(cityTemperature)
            .build();
    publisher.publishMessages();
  }

  /** Generate the device id based on the current date. */
  protected void generateDeviceId() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    deviceId = "dev-" + sdf.format(new Date()) + "-" + UUID.randomUUID().toString();
  }

  /** Create the canonical IoT device registry path on GCP. */
  protected String generateRegistryPath() throws ClientException {
    return String.format(
        "projects/%s/locations/%s/registries/%s",
        getOptions().getGcpProjectId(),
        getOptions().getGcpRegion(),
        getOptions().getRegistryName());
  }

  /** Read public key and generate device credentials. */
  private DeviceCredential loadCredentials() throws ClientException {
    try {
      PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
      String key =
          Files.toString(new File(getOptions().getRsaCertificateFilePath()), Charsets.UTF_8);
      publicKeyCredential.setKey(key);
      publicKeyCredential.setFormat(KEY_FORMAT_RSA_X509_PEM);
      DeviceCredential deviceCredential = new DeviceCredential();
      deviceCredential.setPublicKey(publicKeyCredential);
      return deviceCredential;
    } catch (IOException e) {
      String message =
          String.format("Failed to load device credentials. Cause : {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    }
  }

  /** Generate the Cloud IoT Core service. */
  private CloudIot createCloudIotService() throws ClientException {
    try {
      logDebug("Generating Cloud IoT Core client...");
      GoogleCredential credential =
          GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
      JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
      HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
      CloudIot service =
          new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
              .setApplicationName(APP_NAME_API)
              .build();
      logDebug("Cloud IoT Core client generated.");
      return service;
    } catch (IOException e) {
      String message =
          String.format("Failed to create Cloud IoT Core service. Cause : {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    } catch (GeneralSecurityException e) {
      String message =
          String.format("Failed to create Cloud IoT Core service. Cause : {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    }
  }

  /** Log a debug message. */
  private void logDebug(String message) throws ClientException {
    LogUtils.writeLog(
        logging,
        message,
        Severity.INFO,
        deviceId,
        LogUtils.CLOUD_LOGGING_DEVICE_SIMULATOR,
        getOptions().getGcpProjectId());
  }

  /** Log an error message. */
  private void logError(String message) throws ClientException {
    LogUtils.writeLog(
        logging,
        message,
        Severity.ERROR,
        deviceId,
        LogUtils.CLOUD_LOGGING_DEVICE_SIMULATOR,
        getOptions().getGcpProjectId());
  }

  protected String getDeviceId() {
    return deviceId;
  }
}
