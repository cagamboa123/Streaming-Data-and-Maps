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

package com.google.cloud.demo.iot.nirvana.servlet.rest;

import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.cloud.demo.iot.nirvana.shared.Constants;
import com.google.cloud.demo.iot.nirvana.shared.Utils;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class that represents a servlet used to interact with Compute Engine (i.e. instance removal) */
public class PostGceRemoval extends HttpServlet {

  private static final long serialVersionUID = 6919710226120460383L;
  private static final Logger LOG = Logger.getLogger(PostGceRemoval.class.getName());

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // manage dev env
    if (!Utils.isGaeProduction()) {
      resp.addHeader("Access-Control-Allow-Origin", Constants.GAE_DEV_ADDRESS);
    }
    // include cookies
    resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
    resp.addHeader("Access-Control-Allow-Credentials", "true");

    // get input parameters
    Map<String, String> params = Utils.getRequestParameters(req);

    // enqueue tasks to delete GCE instances
    Utils.enqueueTask(
        Constants.GAE_QUEUE_NAME_GCE,
        Constants.GAE_TASK_GCE_INSTANCE_REMOVAL,
        Constants.GAE_TASK_GCE_INSTANCE_REMOVAL_BASE_NAME + UUID.randomUUID().toString(),
        Method.POST,
        params,
        0);

    // enqueue tasks to delete Cloud IoT Core devices
    Utils.enqueueTask(
        Constants.GAE_QUEUE_NAME_GCE,
        Constants.GAE_TASK_CLOUD_IOT_CORE_DEVICE_REMOVAL,
        Constants.GAE_TASK_CLOUD_IOT_CORE_DEVICE_REMOVAL_BASE_NAME + UUID.randomUUID().toString(),
        Method.POST,
        params,
        0);

    // return data
    RestResponse restResponse = new RestResponse();
    Gson gson = new Gson();

    resp.setContentType(com.google.common.net.MediaType.JSON_UTF_8.toString());
    restResponse.setCode(javax.servlet.http.HttpServletResponse.SC_OK);
    restResponse.setMessage("Task enqueued");
    resp.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
    resp.getWriter().println(gson.toJson(restResponse));

    return;
  }
}
