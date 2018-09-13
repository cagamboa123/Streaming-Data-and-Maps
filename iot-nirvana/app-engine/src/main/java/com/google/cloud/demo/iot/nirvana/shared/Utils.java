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

package com.google.cloud.demo.iot.nirvana.shared;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/** Class that describes functions used throughout the application */
public class Utils {

  private static final Logger LOG = Logger.getLogger(Utils.class.getName());

  /**
   * Convert an object to JSON
   *
   * @param obj the object to be encoded
   * @return encoded object
   */
  public static String toJSON(Object obj) {
    Gson gson = new Gson();
    return gson.toJson(obj);
  }

  /**
   * Convert a JSON string to an Object
   *
   * @param json the json to be converted
   * @param destinationClass the object to be generated
   * @return encoded object
   */
  public static <T> T fromJSON(String json, Class<T> destinationClass) {
    Gson gson = new Gson();
    return gson.fromJson(json, destinationClass);
  }

  /**
   * Check if the app is running in production or in local dev
   *
   * @return true if it is in production, false otherwise
   */
  public static boolean isGaeProduction() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
  }

  /**
   * Check if the running environment is the dev
   *
   * @return true if it is the dev, false otherwise
   */
  public static boolean isGaeDevEnvironment() {
    return !isGaeProduction();
  }

  /**
   * Get input parameters if they are stored in JSON format
   *
   * @param req
   * @return
   * @throws IOException
   */
  public static Map<String, String> getRequestParameters(HttpServletRequest req)
      throws IOException {
    // read input data
    InputStream is = req.getInputStream();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int r = 0;
    while (r >= 0) {
      r = is.read(buf);
      if (r >= 0) {
        os.write(buf, 0, r);
      }
    }

    String inputParams = new String(os.toByteArray(), "UTF-8");
    Gson gson = new Gson();

    Type type = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> map = gson.fromJson(inputParams, type);

    return map;
  }

  /**
   * Get execution task of a task
   *
   * @param req the request of the task
   * @return the execution time of a task if exist
   */
  public static int getExecutionCount(HttpServletRequest req) {
    String header = req.getHeader("X-AppEngine-TaskExecutionCount");
    if (header != null && !header.isEmpty()) {
      return Integer.parseInt(header);
    }
    return 0;
  }

  /**
   * Get execution task name
   *
   * @param req the request of the task
   * @return the execution time of a task if exist
   */
  public static String getExecutionName(HttpServletRequest req) {
    String header = req.getHeader("X-AppEngine-TaskName");
    if (header == null) {
      return null;
    } else {
      return header;
    }
  }

  /**
   * Convert a String array in a single string where all tokens are separated by a comma
   *
   * @param input the String array that you want to split
   * @param tokenSeparator the character that you want to use as token separator
   * @return a string containing all elements of the input array separated by the token separator
   *     character
   */
  public static String fromArrayToString(String[] input, char tokenSeparator) {
    StringBuilder result = new StringBuilder("");
    int i = 0;
    for (String token : input) {
      if (i > 0) {
        result.append(tokenSeparator);
      }
      result.append(token);
      i++;
    }
    return result.toString();
  }

  /**
   * Enqueue a task into Google App Engine
   *
   * @param queueName the name of the queue where to enqueue the task
   * @param taskUrl the URL of the task to be enqueued
   * @param taskName the name of the task to be enqueued
   * @param method the method to use (POST/GET)
   * @param parametersMap the parameters to be added to the task
   * @param delay the eventual delay to add to the task
   */
  public static void enqueueTask(
      String queueName,
      String taskUrl,
      String taskName,
      Method method,
      Map<String, String> parametersMap,
      long delay) {

    // prepare task options
    final TaskOptions taskOptions =
        TaskOptions.Builder.withUrl(taskUrl).taskName(taskName).method(method);

    // add parameters
    for (String key : parametersMap.keySet()) {
      taskOptions.param(key, parametersMap.get(key));
    }

    // add eventual delay
    if (delay > 0) {
      taskOptions.countdownMillis(delay);
    }

    // create the queue
    final Queue queue = QueueFactory.getQueue(queueName);

    Callable<Boolean> callable =
        new Callable<Boolean>() {
          public Boolean call() throws Exception {
            queue.add(taskOptions);
            return true;
          }
        };

    Retryer<Boolean> retryer =
        RetryerBuilder.<Boolean>newBuilder()
            .retryIfException()
            .withWaitStrategy(WaitStrategies.exponentialWait(100, 5, TimeUnit.MINUTES))
            .withStopStrategy(StopStrategies.stopAfterAttempt(5))
            .build();
    try {
      retryer.call(callable);
    } catch (RetryException e) {
      LOG.warning("enqueueTask() failed.\nStackTrace: " + Throwables.getStackTraceAsString(e));
    } catch (ExecutionException e) {
      LOG.warning("enqueueTask() failed.\nStackTrace: " + Throwables.getStackTraceAsString(e));
    }

    return;
  }

  /**
   * Get headers of a HttpServletRequest
   *
   * @param request the request to be parsed
   * @return map of request headers
   */
  public static Map<String, String> getHeadersInfo(HttpServletRequest request) {
    Map<String, String> map = new HashMap<String, String>();
    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String key = (String) headerNames.nextElement();
      String value = request.getHeader(key);
      map.put(key, value);
    }
    return map;
  }

  /**
   * Get value of a specific cookie in the request
   *
   * @param requestthe request that contains the cookie
   * @param cookieName the name of the cookie that you want to get
   * @return the value of the cookie in the request
   */
  public static String getCookieValue(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(cookieName)) {
          return cookie.getValue();
        }
      }
    }
    // no cookie found
    return null;
  }

  /**
   * Get JSON payload
   *
   * @param request the request that has to be processed
   * @return payload of the request
   * @throws IOException
   */
  public static JsonObject getJsonPayload(HttpServletRequest request) throws IOException {
    JsonParser jsonParser = new JsonParser();
    String body = null;
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;

    try {
      InputStream inputStream = request.getInputStream();
      if (inputStream != null) {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        char[] charBuffer = new char[128];
        int bytesRead = -1;
        while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
          stringBuilder.append(charBuffer, 0, bytesRead);
        }
      } else {
        stringBuilder.append("");
      }
    } catch (IOException ex) {
      throw ex;
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException ex) {
          throw ex;
        }
      }
    }

    body = stringBuilder.toString();
    return jsonParser.parse(body).getAsJsonObject();
  }

  /**
   * Convert a bytes array into a HEX string
   *
   * @param bytes the bytes array that you want to convert
   * @return an HEX string representing the bytes array
   */
  public static String toHexString(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
      String hex = Integer.toHexString(0xFF & bytes[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }

    return hexString.toString().toUpperCase();
  }

  /**
   * Read property from property file
   *
   * @param ctx ServletContext
   * @param propertyName the name of the property that you want to read
   * @return the value of the property
   */
  public static String getAppEngineProperty(ServletContext ctx, String propertyName) {
    try {
      InputStream is = ctx.getResourceAsStream(Constants.APP_ENGINE_PROPERTIES_FILE);
      Properties props = new Properties();
      props.load(is);
      return props.getProperty(propertyName);
    } catch (Exception ex) {
      LOG.severe(Throwables.getStackTraceAsString(ex));
    }
    return null;
  }

  /**
   * Compute MD5 of input value
   *
   * @param inputValue the value for which you want to compute MD5 hash
   * @return MD5 hash of the input string
   */
  public static String md5(String inputValue) {
    try {
      byte[] bytesOfMessage = inputValue.getBytes("UTF-8");
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] thedigest = md.digest(bytesOfMessage);
      return Utils.toHexString(thedigest);
    } catch (Exception ex) {
      LOG.severe(Throwables.getStackTraceAsString(ex));
    }
    return null;
  }
}
