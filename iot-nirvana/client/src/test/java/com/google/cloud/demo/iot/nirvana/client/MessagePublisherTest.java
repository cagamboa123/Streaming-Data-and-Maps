package com.google.cloud.demo.iot.nirvana.client;

import static org.junit.Assert.assertEquals;

import com.google.cloud.demo.iot.nirvana.common.CityTemperature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MessagePublisher}. */
@RunWith(JUnit4.class)
public final class MessagePublisherTest {

  MessagePublisher publisher;

  String deviceId = "dev-20180514-123ertfs-5kjl-67gh-lk9j-endloptrh48j2";

  @Before
  public void setUp() throws Exception {
    String argsComplete[] = {
      "-projectId", "my-project",
      "-region", "us-central1-f",
      "-pubSubTopicName", "projects/my-project/topics/my-topic",
      "-registryName", "my-registry",
      "-rsaCertificateFilePath", "/tmp/iot-core/rsa_cert_0.pem",
      "-privateKey", "/tmp/iot-core/rsa_private_0_pkcs8",
      "-cityIndex", "1"};
    ClientOptions options = ClientOptions.newBuilder().build();
    options.parse(argsComplete);
    publisher = MessagePublisher.newBuilder()
        .setOptions(options)
        .setDeviceId(deviceId)
        .setCityTemperature(new CityTemperature())
        .build();
  }

  @Test
  public void generateMqttTopic_resultOk() throws Exception {
    assertEquals(
        publisher.generateMqttTopic(),
        "/devices/dev-20180514-123ertfs-5kjl-67gh-lk9j-endloptrh48j2/events");
  }

  @Test
  public void generateMqttClientId_resultOk() throws Exception {
    assertEquals(
        publisher.generateMqttClientId(),
        "projects/my-project/locations/us-central1-f/registries/my-registry/devices/dev-20180514-123ertfs-5kjl-67gh-lk9j-endloptrh48j2");
  }

  @Test
  public void generateMqttServerAddress_resultOk() throws Exception {
    assertEquals(publisher.generateMqttServerAddress(), "ssl://mqtt.googleapis.com:8883");
  }

}
