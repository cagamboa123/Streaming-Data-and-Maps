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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.InstanceList;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.cloud.demo.iot.nirvana.shared.Constants;
import com.google.cloud.demo.iot.nirvana.shared.Utils;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class that represents a servlet used to interact with Compute Engine (i.e. instance creation) */
public class GetPostGce extends HttpServlet {

  private static final long serialVersionUID = 6919710226120460383L;
  private static final Logger LOG = Logger.getLogger(GetPostGce.class.getName());

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

    int numInstancesToBootsrap =
        Integer.parseInt(params.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NUMBER));

    // checks how many instances you have to setup
    for (int i = 0; i < numInstancesToBootsrap; i++) {
      params.put(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NUMBER, "" + i);
      // enqueue tasks
      Utils.enqueueTask(
          Constants.GAE_QUEUE_NAME_GCE,
          Constants.GAE_TASK_GCE_INSTANCE_CREATION,
          Constants.GAE_TASK_GCE_INSTANCE_CREATION_BASE_NAME + UUID.randomUUID().toString(),
          Method.POST,
          params,
          0);
    }

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

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      // manage dev env
      if (!Utils.isGaeProduction()) {
        resp.addHeader("Access-Control-Allow-Origin", Constants.GAE_DEV_ADDRESS);
      }
      // include cookies
      resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
      resp.addHeader("Access-Control-Allow-Credentials", "true");

      // create default Google AppEngine credential
      GoogleCredential credential =
          GoogleCredential.getApplicationDefault().createScoped(ComputeScopes.all());

      // access to Compute Engine
      // Create compute engine object for listing instances
      Compute compute =
          new Compute.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, null)
              .setApplicationName(Constants.APPLICATION_NAME)
              .setHttpRequestInitializer(credential)
              .build();

      // list instances
      String nextPageToken = null;
      int instancesCounter = 0;
      InstanceList instanceList =
          compute
              .instances()
              .list(Constants.GAE_APP_ID, Constants.GCE_MACHINE_DEFAULT_ZONE)
              .execute();
      nextPageToken = instanceList.getNextPageToken();
      if (instanceList.getItems() != null) {
        instancesCounter += instanceList.getItems().size();
      }
      // if there are more token to elaborate
      while (nextPageToken != null) {
        // get instances
        instanceList =
            compute
                .instances()
                .list(Constants.GAE_APP_ID, Constants.GCE_MACHINE_DEFAULT_ZONE)
                .setPageToken(nextPageToken)
                .execute();
        // save instance information
        if (instanceList.getItems() != null) {
          instancesCounter += instanceList.getItems().size();
        }
        // update nextPageToken
        nextPageToken = instanceList.getNextPageToken();
      }

      // return data
      RestResponse restResponse = new RestResponse();
      Gson gson = new Gson();

      resp.setContentType(com.google.common.net.MediaType.JSON_UTF_8.toString());
      restResponse.setCode(javax.servlet.http.HttpServletResponse.SC_OK);
      restResponse.setMessage("" + instancesCounter);
      resp.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
      resp.getWriter().println(gson.toJson(restResponse));

      return;
    } catch (Exception ex) {
      LOG.warning(Throwables.getStackTraceAsString(ex));
      RestResponse restResponse = new RestResponse();
      Gson gson = new Gson();
      LOG.warning(Constants.HTML_CODE_400_BAD_REQUEST);
      restResponse.setCode(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
      restResponse.setMessage(Constants.HTML_CODE_400_BAD_REQUEST);
      resp.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().println(gson.toJson(restResponse));
      return;
    }
  }
}
