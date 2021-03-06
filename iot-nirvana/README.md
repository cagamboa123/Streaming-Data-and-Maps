# IoT Nirvana

This solution was built with the purpose of demonstrating and end-to-end
Internet of Things architecture running on Google Cloud Platform. The purpose of
the solution is to simulate the collection of temperature measures from sensors
distributed all over the world and to follow temperature evolution by city in
real time. This document will guide you through the necessary steps to set up
the entire solution on Google Cloud Platform (GCP).

## Architecture

The image below contains a high level diagram of the solution.
![](img/architecture.png)

The following components are represented on the diagram:

1. Temperature sensors are simulated by running IoT Java clients on Google Compute
   Engine
2. The sensors send temperature data to an IoT Core registry running on GCP
3. The IoT Core registry publishes it into a PubSub topic
4. A streaming Dataflow pipeline is capturing the temperature data in real time
   by subscribing to and reading from the PubSub topic
5. Temperature data is pushed into BigQuery for analytics purposes
6. Temperature data is also saved to Datastore for real time querying
7. Temperature is displayed in real time in a Web AppEngine application
8. All components are logging data to Stackdriver

## Bootstrapping

As a pre-requisite you will need a GCP project to which you have owner rights
in order to facilitate the setup of the solution. In the remainder of this guide
this project's identifier will be referred to as **[PROJECT_ID]**.

Enable the following APIs in your project:

* [Cloud Pub/Sub API](https://pantheon.corp.google.com/apis/api/pubsub.googleapis.com)
* [DataFlow API](https://pantheon.corp.google.com/apis/api/dataflow.googleapis.com)
* [Google Cloud IoT API](https://pantheon.corp.google.com/apis/library/cloudiot.googleapis.com)

In order to run the simulation in ideal conditions, with 10 virtual machines,
please request an increase of your CPU quota to 80 vCPU. This is however
optional.

Run the **GCP_Environment_Setup.sh** script with the following parameters, in
this order, to create the corresponding resources in your GCP project:

* **[PROJECT_ID]** - the identifier of your project, which will be used to
  create a Google Cloud Storage bucket; this will guarantee the uniqueness of
  your bucket's name across the entire Google Cloud Platform
* **[PUBSUB_TOPIC]** - a name of your choice for the PubSub topic into which the
  IoT registry will push the temperature data it receives from the IoT devices
* **[PUBSUB_SUBSCRIPTION]** - a name for the default subscription that will be
  created for the topic
* **[IOT_REGISTRY]** - a name for the IoT registry that will be created
* **[BIGQUERY_DATASET]** - a name for the BigQuery dataset that will be created

In addition, the script also creates an Debian image with Java pre-installed,
called **debian9-java8-img** that will be used to run the Java programs
simulating temperature sensors.

## Dataflow pipeline

In order to run the Dataflow pipeline execute the `run_oncloud.sh` script in the
`/pipeline` folder with the following parameters:

* **[PROJECT_ID]** - your project's identifier
* **[BUCKET_NAME]** - the name of the bucket created by the bootstrapping
  script, identical to your projet's identifier, **[PROJECT_ID]**, where the
  Dataflow pipeline's binary package will be stored
* **[PUBSUB_TOPIC]** - the name of the PubSub topic created by the bootstrapping
  script, from which the Dataflow pipeline will read the temperature data;
  please note that this isn't the topic's canonical name, but instead the name
  relative to your project
* **[BIGQUERY_TABLE]** - a name for the BigQuery table where Dataflow will save
  the temperature data; the format of this parameter must follow the rule
  **[BIGQUERY_DATASET].[TABLE_NAME]**

Example:

`run_oncloud.sh my-project my-bucket my-topic my-dataset.my-table`

## Temperature sensor

The first action is to compile the Java client simulating the temperature sensor
and generate the JAR package containing the compiled binary and its
dependencies. Run the following command in the `/client`folder:

`mvn package`

The second action is to copy the JAR package containing the client binaries on
Google Cloud Storage in the bucket previously created. Run the following command
in the `/client` folder:

`gsutil cp target/google-cloud-demo-iot-nirvana-client-jar-with-dependencies.jar gs://[BUCKET_NAME]/client`

Check that the JAR file has been correctly copied in the Google Cloud Storage
bucket with the following command:

`gsutil ls gs://[BUCKET_NAME]/client/google-cloud-demo-iot-nirvana-client-jar-with-dependencies.jar`

## AppEngine Web frontend

The following steps will allow you to set up and run on AppEngine the Web
frontend that allows to visualize in real time the temperature data captured
from the temperature sensors:

1. Enable the [Maps Javascript API](https://pantheon.corp.google.com/apis/library/maps-backend.googleapis.com)
2. In the *Credentials* section of the Maps Javascript API generate the API key
   that will be used by the Web frontend to call Google Maps. This key will be
   referred to as **[MAPS_API_KEY]** further in the document.
3. Run the `gcloud app create` command to create the Google AppEngine
   application
4. Modify the `/pom.xml` file in the `/app-egine` folder:
   * Update the `<app.id/>` node with the **[PROJECT_ID]** of your GCP project
   * Update the `<app.version/>` with the desired version of the application
5. Modify the `src/main/webapp/startup.sh`file in the `/app-egine` folder by
   updating the variables below. This is the startup script of the Virtual
   Machines that will be created from the image **debian9-java8-img** and it
   creates 10 instances of the Java client simulating a temperature sensor.
   * PROJECT_ID - your GCP project's identifier, **[PROJECT_ID]**
   * BUCKET_NAME - name of the Google Cloud Storage bucket created by the
     bootstrapping script
   * TOPIC_NAME - name of the topic created by the bootstrapping script
   * REGISTRY_NAME - name of the IoT Core registry created by the bootstrapping
     script
6. Copy the `startup.sh` file in the Google Cloud Storage bucket by running the following
   command in the `/app-engine` folder:
   `gsutil cp src/main/webapp/startup.sh gs://[BUCKET_NAME]/`
7. Modify the `src/main/webapp/config/client.properties` file in the
   `/app-engine` folder by updating the values of the following parameters:
   * GCS_BUCKET- name of the Google Cloud Storage bucket created by the
     bootstrapping script
   * GCE_METADATA_STARTUP_VALUE - path on Google Cloud Storage to the startup
     script edited at the previous step, gs://[BUCKET_NAME]/startup.sh
   * GCP_CLOUD_IOT_CORE_REGISTRY_NAME- name of the IoT Core registry created by
     the bootstrapping script
8. Update the `src/main/webapp/index.html` file in the `/app-engine` folder by
   replacing the **[MAPS_API_KEY]** text with the actual value of the Google
   Maps API key generated at step 2.
9. Deploy the frontend Web application on AppEngine by running the following
   command in the `/app-engine` folder:
   `mvn appengine:update`

## Testing

In order to test the end to end solution, it is necessary first to start the
temperature sensors simulation. Follow the steps below to achieve this:

* Go to the following address in your web browser, which will display the map
  of the Earth with 3 buttons at the bottom: **Start**, **Update**, **Stop**
  `https://[YOUR_PROJECT_ID].appspot.com/index.html`
* Click on the **Start** button at the bottm left of the page (this also
  enables the buttons **Update** and **Stop**)
* The VM instances being launched ar visible in the Google Cloud Console under
  (Compute Engine)[https://pantheon.corp.google.com/compute/instances]

In order to visualize temperature data in real time on Google Maps do the
following:

* Click on the **Update** button at the bottom center of the page
  `https://[YOUR_PROJECT_ID].appspot.com/index.html`. This will display on the
  map test cities used for simulating the temperature sensors.
* Run the following SQL query in BigQuery to retrieve the most recent cities for
  which data is available:
  `SELECT City, Time FROM ``[BIGQUERY_DATASET].[TABLE_NAME]`` ORDER BY 2 DESC LIMIT 10`
* Locate on the map one of the cities returned by the query and click on the
  city icon to visualise the temperatures graph.

To stop the simulation click on the **Stop** button at the bottom right of the
page `https://[YOUR_PROJECT_ID].appspot.com/index.html`.
