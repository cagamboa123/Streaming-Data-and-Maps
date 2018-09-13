package com.google.cloud.demo.iot.nirvana.datastore.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Message}. */
@RunWith(JUnit4.class)
public final class MessageTest {

  public void constructor_resultOk() throws Exception {
    Message message = new Message("8762723AE028CAA144CBF8B7069003C3", 31, 1523999305000L);
    assertEquals(message.getId(), "8762723AE028CAA144CBF8B7069003C3");
    assertTrue(message.getTemperature() == 31);
    assertTrue(message.getTimestamp() == 1523999305000L);
  }

  @Test
  public void setId_resultDirectOk() throws Exception {
    Message message = new Message();
    message.setId("8762723AE028CAA144CBF8B7069003C3");
    assertEquals(message.getId(), "8762723AE028CAA144CBF8B7069003C3");
  }

  @Test
  public void setTemperature_resultOkPositive() throws Exception {
    Message message = new Message();
    message.setTemperature(31);
    assertTrue(message.getTemperature() == 31);
  }

  @Test
  public void setTemperature_resultOkNegative() throws Exception {
    Message message = new Message();
    message.setTemperature(-31);
    assertTrue(message.getTemperature() == -31);
  }

  @Test
  public void setTimestamp_resultOkPositive() throws Exception {
    Message message = new Message();
    message.setTimestamp(1523999305000L);
    assertTrue(message.getTimestamp() == 1523999305000L);
  }
}
