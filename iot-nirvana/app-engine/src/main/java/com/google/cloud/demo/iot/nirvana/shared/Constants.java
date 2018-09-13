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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.utils.SystemProperty;

/** Class containing constants to be used throughout the application */
public class Constants {

  /** GAE APPLICATION * */
  public static final String GAE_DEV_ADDRESS = "http://localhost:8080";

  public static final String GAE_APP_ID = SystemProperty.applicationId.get();
  public static final String GAE_APP_SUFFIX = ".appspot.com";
  public static final String PROTOCOL_HTTP = "http://";
  public static final String PROTOCOL_HTTPS = "https://";
  public static final String GAE_APP_URL = PROTOCOL_HTTPS + GAE_APP_ID + GAE_APP_SUFFIX;
  public static final String APP_ENGINE_PROPERTIES_FILE = "/config/client.properties";

  /** OAUTH 2.0 Parameters * */
  public static final String APPLICATION_NAME = "IoT Nirvana V3 - Cloud IoT Core Update";

  public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  public static final JsonFactory JSON_FACTORY = new JacksonFactory();
  public static final String COOKIE_AUTH_NAME = "can";
  public static final String GOOGLE_ID_TOKEN = "git";

  /** REST APIs * */
  public static final String API_AUTHORIZATION_HEADER = "Authorization";

  public static final String API_AUTHORIZATION_HEADER_TYPE = "Bearer";
  public static final String API_PARAM_SECURITY_CODE = "sc";
  public static final String API_PARAM_CITY_TEMPERATURE = "id";
  public static final String API_PARAM_CITY_TEMPERATURE_NUM_RECORD = "numRecord";
  public static final String HTML_CODE_400_BAD_REQUEST =
      "The request received from the server was not correct";
  public static final String HTML_CODE_401_UNAUTHORIZED_MESSAGE =
      "API requires valid authentication";
  public static final String HTML_CODE_200_OK_MESSAGE = "OK";
  public static final int MAX_RETRY = 5;
  public static final String NEXT_PAGE_TOKEN = "nextPageToken";

  /** Google Compute Engine * */
  public static final String GCE_MACHINE_DEFAULT_ZONE = "us-central1-a";

  public static final String GCE_MACHINE_TYPE_FULL_URL =
      "https://www.googleapis.com/compute/v1/projects/"
          + GAE_APP_ID
          + "/zones/xxxZONExxx/machineTypes/xxxMACHINE_TYPExxx";
  public static final String GCE_MACHINE_TYPE_N1_HIGHCPU_2 = "n1-highcpu-2";
  public static final String GCE_MACHINE_TYPE_F1_MICRO = "f1-micro";
  public static final String GCE_LINUX_BOOT_IMG =
      "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-8-jessie-v20161027";
  public static final String GCE_NETWORK_TYPE =
      "https://www.googleapis.com/compute/v1/projects/" + GAE_APP_ID + "/global/networks/default";
  public static final String GCE_LINUX_JAVA =
      "https://www.googleapis.com/compute/v1/projects/"
          + GAE_APP_ID
          + "/global/images/debian9-java8-img";
  public static final int GCE_MAX_INSTANCES = 1;
  public static final String GCE_METADATA_STARTUP_KEY = "startup-script-url";
  public static final String GCE_METADATA_MESSAGE_POST = "message-post";
  public static final String GCE_METADATA_MESSAGE_POST_VALUE = "api/message/post";
  public static final String GCE_METADATA_MESSAGE_POST_NUMBER = "message-post-number";
  public static final String GCE_METADATA_MESSAGE_POST_NUMBER_VALUE = "100";
  public static final String GCE_METADATA_INSTANCE_NAME = "instance-name";
  public static final String GCE_METADATA_INSTANCE_ZONE = "instance-zone";
  public static final String GCE_METADATA_INSTANCE_START_METHOD_NAME = "instance-start";
  public static final String GCE_METADATA_INSTANCE_START_METHOD_VALUE = "api/gce/instance/start";
  public static final String GCE_METADATA_INSTANCE_STOP_METHOD_NAME = "instance-stop";
  public static final String GCE_METADATA_INSTANCE_STOP_METHOD_VALUE = "api/gce/instance/stop";
  public static final String GCE_METADATA_TOPIC_NAME = "topic";
  public static final String GCE_NETWORK_EMPHERAL = "ONE_TO_ONE_NAT";
  public static final long GCE_LIST_MAX_NUMBER = 1;
  public static final String GCE_DEFAULT_INSTANCE_NAME = "publisher";

  /** Google App Engine * */
  public static final String GAE_QUEUE_DEFAULT_NAME = "default";

  public static final String GAE_QUEUE_NAME_GCE = "gce";

  public static final String GAE_TASK_GCE_INSTANCE_CREATION = "/task/gce/instance/creation";
  public static final String GAE_TASK_GCE_INSTANCE_REMOVAL = "/task/gce/instance/removal";
  public static final String GAE_TASK_CLOUD_IOT_CORE_DEVICE_REMOVAL =
      "/task/cloudiotcore/device/removal";
  public static final String GAE_TASK_GCE_INSTANCE_CREATION_BASE_NAME = "gce-creation-";
  public static final String GAE_TASK_GCE_INSTANCE_REMOVAL_BASE_NAME = "gce-removal-";
  public static final String GAE_TASK_CLOUD_IOT_CORE_DEVICE_REMOVAL_BASE_NAME =
      "cloud-iot-core-device-removal-";
  public static final String GAE_TASK_GCE_PARAM_NAME_INSTANCE_NAME = "instanceName";
  public static final String GAE_TASK_GCE_PARAM_NAME_INSTANCE_TYPE = "instanceType";
  public static final String GAE_TASK_GCE_PARAM_NAME_INSTANCE_ZONE = "instanceZone";
  public static final String GAE_TASK_GCE_PARAM_NAME_INSTANCE_NUMBER = "instance-number";
  public static final String GAE_TASK_GCE_PARAM_NAME_INSTANCE_MESSAGE_POST_NUMBER =
      "messagePostNumber";
}
