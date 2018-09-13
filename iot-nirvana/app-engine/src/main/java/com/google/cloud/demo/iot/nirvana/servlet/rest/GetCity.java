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

import com.google.cloud.demo.iot.nirvana.datastore.DatastoreService;
import com.google.cloud.demo.iot.nirvana.datastore.entity.City;
import com.google.cloud.demo.iot.nirvana.datastore.entity.CityTemperature;
import com.google.cloud.demo.iot.nirvana.shared.Constants;
import com.google.cloud.demo.iot.nirvana.shared.Utils;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class that represents a servlet used to get list of cities */
public class GetCity extends HttpServlet {

  private static final long serialVersionUID = 6919710226120460383L;
  private static final Logger LOG = Logger.getLogger(GetCity.class.getName());

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // manage dev env
    if (!Utils.isGaeProduction()) {
      resp.addHeader("Access-Control-Allow-Origin", Constants.GAE_DEV_ADDRESS);
    }
    // include cookies
    resp.addHeader("Access-Control-Allow-Credentials", "true");

    // return data
    RestResponse restResponse = new RestResponse();
    Gson gson = new Gson();

    // get all cities
    DatastoreService<City> dsCity = new DatastoreService<City>(City.class);
    DatastoreService<CityTemperature> dsCityTemperature =
        new DatastoreService<CityTemperature>(CityTemperature.class);
    List<City> cities = dsCity.list();
    List<City> citiesToMaintain = new ArrayList<City>();
    for (City city : cities) {
      // remove all cities that do not have temperatures
      if (dsCityTemperature.listKeysForParent(Key.create(City.class, city.getId())) != null) {
        citiesToMaintain.add(city);
      }
    }

    resp.setContentType(com.google.common.net.MediaType.JSON_UTF_8.toString());
    restResponse.setCode(javax.servlet.http.HttpServletResponse.SC_OK);
    restResponse.setMessage(gson.toJson(citiesToMaintain));
    resp.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
    resp.getWriter().println(gson.toJson(restResponse));

    return;
  }
}
