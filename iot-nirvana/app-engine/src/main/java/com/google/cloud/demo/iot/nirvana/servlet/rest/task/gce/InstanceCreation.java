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

package com.google.cloud.demo.iot.nirvana.servlet.rest.task.gce;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.AttachedDiskInitializeParams;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.ServiceAccount;
import com.google.cloud.demo.iot.nirvana.servlet.rest.task.AbstractTask;
import com.google.cloud.demo.iot.nirvana.shared.Constants;
import com.google.cloud.demo.iot.nirvana.shared.Utils;
import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

/** Class that represents a task to generate instances on Google Compute Engine */
public class InstanceCreation extends AbstractTask {

  private static final long serialVersionUID = -1981951826229863468L;
  private static final Logger LOG = Logger.getLogger(InstanceCreation.class.getName());

  @Override
  protected void process(Map<String, String[]> parameterMap) throws Throwable {

    // define variables
    String baseInstanceName = Constants.GCE_DEFAULT_INSTANCE_NAME;
    if (parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NAME) != null) {
      baseInstanceName = parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NAME)[0];
    }

    String instanceName = "";
    String instanceType = Constants.GCE_MACHINE_TYPE_F1_MICRO;
    ;
    if (parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_TYPE) != null) {
      instanceType = parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_TYPE)[0];
    }
    String instanceZone = Constants.GCE_MACHINE_DEFAULT_ZONE;
    if (parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_ZONE) != null) {
      instanceZone = parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_ZONE)[0];
    }

    String instanceNumber = Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NUMBER;
    if (parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NUMBER) != null) {
      instanceNumber = parameterMap.get(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NUMBER)[0];
    }

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

    /** INSTANCE NAME */
    // generate random instance_id
    Random rnd = new Random();
    long instanceId = rnd.nextLong();
    instanceName = baseInstanceName + "-" + UUID.randomUUID().toString();

    /** DISK */
    // define disks
    AttachedDiskInitializeParams newDiskInitParams = new AttachedDiskInitializeParams();
    newDiskInitParams.setDiskName("disk-" + instanceId);
    newDiskInitParams.setSourceImage(Constants.GCE_LINUX_JAVA);

    AttachedDisk newDisk = new AttachedDisk();
    newDisk.setBoot(true); // boot the instance using this disk
    newDisk.setAutoDelete(true); // delete the disk when the associated instance will be deleted
    newDisk.setInitializeParams(newDiskInitParams);

    List<AttachedDisk> disks = new ArrayList<AttachedDisk>();
    disks.add(newDisk);

    /** NETWORK */
    // define network
    NetworkInterface net = new NetworkInterface();
    net.setNetwork(Constants.GCE_NETWORK_TYPE);
    // define access config (only needed for public IP)
    AccessConfig ac = new AccessConfig();
    ac.setType(Constants.GCE_NETWORK_EMPHERAL);
    List<AccessConfig> acs = new ArrayList<AccessConfig>();
    acs.add(ac);
    net.setAccessConfigs(acs);
    List<NetworkInterface> networks = new ArrayList<NetworkInterface>();
    networks.add(net);

    /** METADATA STARTUP SCRIPT */
    // Define metadata items

    // startup script
    // read the path of STARTUP file

    Metadata.Items itemStartupScript = new Metadata.Items();
    itemStartupScript.setKey(Constants.GCE_METADATA_STARTUP_KEY);
    itemStartupScript.setValue(
        Utils.getAppEngineProperty(this.getServletContext(), "GCE_METADATA_STARTUP_VALUE"));
    // input instance name
    Metadata.Items instanceNumberMetadata = new Metadata.Items();
    instanceNumberMetadata.setKey(Constants.GAE_TASK_GCE_PARAM_NAME_INSTANCE_NUMBER);
    instanceNumberMetadata.setValue(instanceNumber);

    // create array of metadata
    List<Metadata.Items> items = new ArrayList<Metadata.Items>();
    items.add(itemStartupScript);
    items.add(instanceNumberMetadata);
    Metadata metadata = new Metadata();
    metadata.setItems(items);

    // define Service Account to be used in the GCE
    // (for a sake of simplicity a full-scopes scope has been used)
    ServiceAccount account = new ServiceAccount();
    account.setEmail("default");
    List<String> scopes = new ArrayList<>();
    scopes.add("https://www.googleapis.com/auth/cloud-platform");
    account.setScopes(scopes);

    /** INSTANCE definition */
    Instance newInstance = new Instance();
    newInstance.setName(instanceName);
    newInstance.setMachineType(
        Constants.GCE_MACHINE_TYPE_FULL_URL
            .replaceAll("xxxZONExxx", instanceZone)
            .replaceAll("xxxMACHINE_TYPExxx", instanceType));
    newInstance.setDisks(disks);
    newInstance.setNetworkInterfaces(networks);
    newInstance.setMetadata(metadata);
    newInstance.setServiceAccounts(Collections.singletonList(account));

    /** BOOTSTRAP machine */
    int retry = 0;
    while (retry < Constants.MAX_RETRY) {
      try {
        compute.instances().insert(Constants.GAE_APP_ID, instanceZone, newInstance).execute();
        retry = 5;
        LOG.info("[Application] machine " + instanceName + " is bootstrapping");
      } catch (Exception ex) {
        // exponential backoff
        LOG.warning(
            "Exception in creating machine - I will retry again - Iteration# "
                + retry
                + "\n"
                + Throwables.getStackTraceAsString(ex));
        try {
          Thread.sleep((1 << retry) * 1000 + rnd.nextInt(1001));
        } catch (Exception exSleep) {;
        }

        retry++;
      }
    }
  }
}
