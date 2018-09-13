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
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class that represents a servlet used to get data of a city (i.e. temperature) */
public class GetCityTemperature extends HttpServlet {

  private static final long serialVersionUID = 6919710226120460383L;
  private static final Logger LOG = Logger.getLogger(GetCityTemperature.class.getName());

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

    // check if parmaters is present
    if (req.getParameter(Constants.API_PARAM_CITY_TEMPERATURE) == null) {
      // error
      LOG.warning(
          Constants.HTML_CODE_400_BAD_REQUEST
              + " - "
              + Constants.API_PARAM_CITY_TEMPERATURE
              + " parameter missing");
      restResponse.setCode(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
      restResponse.setMessage(Constants.HTML_CODE_400_BAD_REQUEST);
      resp.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().println(gson.toJson(restResponse));
      return;
    }
    if (req.getParameter(Constants.API_PARAM_CITY_TEMPERATURE_NUM_RECORD) == null) {
      // error
      LOG.warning(
          Constants.HTML_CODE_400_BAD_REQUEST
              + " - "
              + Constants.API_PARAM_CITY_TEMPERATURE_NUM_RECORD
              + " parameter missing");
      restResponse.setCode(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
      restResponse.setMessage(Constants.HTML_CODE_400_BAD_REQUEST);
      resp.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().println(gson.toJson(restResponse));
      return;
    }

    // create CityKey
    Key<City> cityKey =
        Key.create(City.class, Utils.md5(req.getParameter(Constants.API_PARAM_CITY_TEMPERATURE)));
    // query last 20 avaialble values
    DatastoreService<CityTemperature> dsCityTemperature =
        new DatastoreService<CityTemperature>(CityTemperature.class);
    String filterKey = "<";
    Key<CityTemperature> filterValue =
        Key.create(CityTemperature.class, new java.util.Date().getTime());
    List<CityTemperature> citiesTemperature =
        dsCityTemperature.getByFilterKey(
            cityKey,
            filterKey,
            filterValue,
            true,
            Integer.parseInt(req.getParameter(Constants.API_PARAM_CITY_TEMPERATURE_NUM_RECORD)));

    resp.setContentType(com.google.common.net.MediaType.JSON_UTF_8.toString());
    restResponse.setCode(javax.servlet.http.HttpServletResponse.SC_OK);
    restResponse.setMessage(gson.toJson(citiesTemperature));
    resp.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
    resp.getWriter().println(gson.toJson(restResponse));

    return;
  }
}
