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

package com.google.cloud.demo.iot.nirvana.servlet.rest.task;

import com.google.cloud.demo.iot.nirvana.shared.Utils;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Abstract class that provides functions to enqueue and manage tasks on Cloud Tasks */
public abstract class AbstractTask extends HttpServlet {

  private static final long serialVersionUID = -5841604863236240751L;
  private static final Logger LOG = Logger.getLogger(AbstractTask.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // get task name
    String taskName = Utils.getExecutionName(req);
    // check execution count
    int executionCount = Utils.getExecutionCount(req);
    try {
      Map<String, String[]> parameterMap = (Map<String, String[]>) req.getParameterMap();

      // print input parameters if DEV
      if (!Utils.isGaeProduction()) {
        StringBuilder parametersOut = new StringBuilder("{");
        int i = 0;
        for (String key : parameterMap.keySet()) {
          if ((i++) > 0) {
            parametersOut.append(",");
          }
          parametersOut.append("\"");
          parametersOut.append(key);
          parametersOut.append("\"");
          parametersOut.append(":");
          parametersOut.append("\"");
          parametersOut.append(parameterMap.get(key)[0]);
          parametersOut.append("\"");
        }
        parametersOut.append("}");
        LOG.info(parametersOut.toString());
      }

      // process the task
      process(parameterMap);
    } catch (Throwable t) {
      LOG.warning(
          "Execution #"
              + executionCount
              + " failed.\nIt will be retried.\nStackTrace: "
              + Throwables.getStackTraceAsString(t));
      if (executionCount == 0) { // write error logs only in case of FIRST error
        LOG.severe("StackTrace: " + Throwables.getStackTraceAsString(t));
      }
      throw new RuntimeException(
          "Error during execution of task: " + taskName + ". It will be retried.");
    }
  }

  /**
   * Abstract method that every subclass has to implement. It is the execution
   *
   * @param parameterMap
   * @throws Throwable
   */
  protected abstract void process(Map<String, String[]> parameterMap) throws Throwable;
}
