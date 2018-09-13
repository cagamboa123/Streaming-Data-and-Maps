package com.google.cloud.demo.iot.nirvana.client;

import com.google.auto.value.AutoValue;
import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import com.google.cloud.demo.iot.nirvana.common.Message;
import com.google.cloud.demo.iot.nirvana.common.TemperatureException;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Severity;
import com.google.gson.Gson;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.joda.time.DateTime;

/** A MQTT client sending temperature data to a device registry on Google Cloud IoT Core */
@AutoValue
public abstract class MessagePublisher {

  static final Logging logging = LoggingOptions.getDefaultInstance().getService();

  private static double MIN_DELTA = -0.05;
  private static double MAX_DELTA = 0.05;
  private static final String CLOUD_IOT_CORE_MQTT_BRIDGE_HOST_NAME = "mqtt.googleapis.com";
  private static final short CLOUD_IOT_CORE_MQTT_BRIDGE_PORT = 8883;

  public abstract String getDeviceId();

  public abstract ClientOptions getOptions();

  public abstract CityTemperature getCityTemperature();

  /** Builder for MessageSender. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setDeviceId(String deviceId);

    public abstract Builder setOptions(ClientOptions options);

    public abstract Builder setCityTemperature(CityTemperature cityTemperature);

    public abstract MessagePublisher build();
  }

  public static Builder newBuilder() {
    return new AutoValue_MessagePublisher.Builder();
  }

  /**
   * Generate and publish a temperature messages to Cloud IoT Core with a 10% variance around a
   * given temperature value and with a frequency of 1 message/second.
   */
  public void publishMessages() throws ClientException, TemperatureException {
    try {
      String mqttTopic = generateMqttTopic();
      MqttClient client = createAndConnectMqttClient();
      while (true) {
        // Create the temperature message to publish
        double variance = MIN_DELTA + (Math.random() * MAX_DELTA);
        Message msg = new Message();
        msg.setId(getCityTemperature().getLat(), getCityTemperature().getLng());
        msg.setTemperature(getCityTemperature().getTemperature() + variance);
        msg.setTimestamp(new java.util.Date().getTime());

        // Create the MQTT message (in this case QoS=1 --> delivered at least once)
        String payload = new Gson().toJson(msg);
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);

        // Publish the message and sleep for 1 second
        client.publish(mqttTopic, message);
        logDebug("Message published: " + message);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          logDebug("Message sending interrupted.");
        }
      }
    } catch (MqttException e) {
      String message = String.format("Failed to send messages. Cause: {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    }
  }

  /** Create a Cloud IoT Core MQTT client and connect to the server. */
  private MqttClient createAndConnectMqttClient() throws ClientException {
    try {
      // Cloud IoT core supports only MQTT 3.1.1
      MqttConnectOptions connectOptions = new MqttConnectOptions();
      connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
      connectOptions.setUserName("notused");
      connectOptions.setPassword(createJwtRsa().toCharArray());
      // Create a client, and connect to the Google MQTT bridge with the defined options
      String mqttServerAddress = generateMqttServerAddress();
      String mqttClientId = generateMqttClientId();
      MqttClient client = new MqttClient(mqttServerAddress, mqttClientId, new MemoryPersistence());
      client.connect(connectOptions);
      return client;
    } catch (MqttException e) {
      String message =
          String.format("Failed to connect to the IoT server. Cause: {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    }
  }

  /** Build the endpoint of the MQTT Cloud IoT Core server. */
  protected String generateMqttServerAddress() {
    return String.format(
        "ssl://%s:%s", CLOUD_IOT_CORE_MQTT_BRIDGE_HOST_NAME, CLOUD_IOT_CORE_MQTT_BRIDGE_PORT);
  }

  /** Create the MQTT identifier of the device sending messages. */
  protected String generateMqttClientId() throws ClientException {
    return String.format(
        "projects/%s/locations/%s/registries/%s/devices/%s",
        getOptions().getGcpProjectId(),
        getOptions().getGcpRegion(),
        getOptions().getRegistryName(),
        getDeviceId());
  }

  /**
   * Create a JWT to authenticate this device. The device will be disconnected after the token
   * expires, and will have to reconnect with a new token. The audience field should always be set
   * to the GCP project id.
   */
  private String createJwtRsa() throws ClientException {
    try {
      DateTime now = new DateTime();
      JwtBuilder jwtBuilder =
          Jwts.builder()
              .setIssuedAt(now.toDate())
              .setExpiration(now.plusMinutes(20).toDate())
              .setAudience(getOptions().getGcpProjectId());
      byte[] keyBytes = Files.readAllBytes(Paths.get(getOptions().getPrivateKey()));
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return jwtBuilder.signWith(SignatureAlgorithm.RS256, kf.generatePrivate(spec)).compact();
    } catch (IOException e) {
      String message = String.format("Cannot create JWT. Cause: {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    } catch (NoSuchAlgorithmException e) {
      String message = String.format("Cannot create JWT. Cause: {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    } catch (InvalidKeySpecException e) {
      String message = String.format("Cannot create JWT. Cause: {}", e.getMessage());
      logError(message);
      throw new ClientException(message, e);
    }
  }

  /** Create the MQTT topic on which the device will publish messages to Cloud IoT Core. */
  protected String generateMqttTopic() {
    return String.format("/devices/%s/events", getDeviceId());
  }

  /** Log a debug message. */
  private void logDebug(String message) throws ClientException {
    LogUtils.writeLog(
        logging,
        message,
        Severity.INFO,
        getDeviceId(),
        LogUtils.CLOUD_LOGGING_DEVICE_SIMULATOR,
        getOptions().getGcpProjectId());
  }

  /** Log an error message. */
  private void logError(String message) throws ClientException {
    LogUtils.writeLog(
        logging,
        message,
        Severity.ERROR,
        getDeviceId(),
        LogUtils.CLOUD_LOGGING_DEVICE_SIMULATOR,
        getOptions().getGcpProjectId());
  }
}
