#!/bin/bash
###
###/*
### * Copyright (C) 2018 Google Inc.
### *
### * Licensed under the Apache License, Version 2.0 (the "License"); you may not
### * use this file except in compliance with the License. You may obtain a copy of
### * the License at
### *
### * http://www.apache.org/licenses/LICENSE-2.0
### *
### * Unless required by applicable law or agreed to in writing, software
### * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
### * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
### * License for the specific language governing permissions and limitations under
### * the License.
### */

# *** Prerequisites ***
# 1) create a GCP project with your preferred ProjectID
# 2) enable billing
# 3) enable PubSub APIs
# 4) enable DataFlow APIs
# 5) enable Cloud IoT Core APIs
# 6) request for more quota: 80 vCPU
#

# check input parameters
if [ $# -lt 5 ]
then
    echo "Following arguments have to be provided:
    1) Project ID
    2) PubSub Topic name
    3) PubSub Subscription name
    4) Cloud IoT registry name
    5) BigQuery Dataset name

    Please note that following preliminary activities need to be performed:
    1) create a GCP project with your preferred ProjectID
    2) enable billing
    3) enable PubSub APIs
    4) enable DataFlow APIs
    5) enable Cloud IoT Core APIs
    6) request for more quota: 80 vCPU
    "
    exit 1
fi

PROJECT_ID=$1
PUBSUB_TOPIC=$2
PUBSUB_SUBSCRIPTION=$3
IOT_REGISTRY=$4
BIGQUERY_DATASET=$5

# login
echo "Executing gcloud auth login"
gcloud auth login
echo "Executing gcloud auth application-default login"
gcloud auth application-default login

# set newly created project
echo "Executing gcloud config set project ${PROJECT_ID}"
gcloud config set project ${PROJECT_ID}

# create a bucket with the name of the project-id
echo "Executing gsutil mb gs://${PROJECT_ID}"
gsutil mb gs://${PROJECT_ID}

#create DataFlow folders
touch delete.me
echo "Executing gsutil cp delete.me gs://${PROJECT_ID}/datafow/"
gsutil cp delete.me gs://${PROJECT_ID}/datafow/
echo "Executing gsutil cp delete.me gs://${PROJECT_ID}/datafow/temp/"
gsutil cp delete.me gs://${PROJECT_ID}/datafow/temp/
echo "Executing gsutil cp delete.me gs://${PROJECT_ID}/datafow/staging/"
gsutil cp delete.me gs://${PROJECT_ID}/datafow/staging/

# create PubSub topic
echo "Executing gcloud beta pubsub topics create ${PUBSUB_TOPIC}"
gcloud pubsub topics create ${PUBSUB_TOPIC}

# create PubSub subscription
echo "Executing gcloud beta pubsub subscriptions create ${PUBSUB_SUBSCRIPTION} --topic ${PUBSUB_TOPIC}"
gcloud pubsub subscriptions create ${PUBSUB_SUBSCRIPTION} --topic ${PUBSUB_TOPIC}

# create IoT registry
echo "Executing gcloud beta iot registries create ${IOT_REGISTRY} --region us-central1 --event-notification-config=topic=projects/${PROJECT_ID}/topics/${PUBSUB_TOPIC}"
gcloud iot registries create ${IOT_REGISTRY} --region us-central1 --event-notification-config=topic=projects/${PROJECT_ID}/topics/${PUBSUB_TOPIC}

# create BigQuery dataset
echo "Executing bq --location=US mk --dataset ${PROJECT_ID}:${BIGQUERY_DATASET}"
bq --location=US mk --dataset ${PROJECT_ID}:${BIGQUERY_DATASET}

# copy VM startup-script into Google Cloud Storage
echo "Executing gsutil cp startup_install_java8.sh gs://${PROJECT_ID}"
gsutil cp startup_install_java8.sh gs://${PROJECT_ID}

# generate a temporary VM that will be used to generate custom image
echo "Executing gcloud compute instances create debian9-java8-img --zone us-central1-f --image-family debian-9 --image-project debian-cloud --metadata startup-script-url=gs://$1/startup_install_java8.sh"
gcloud compute instances create debian9-java8-img --zone us-central1-f --image-family debian-9 --image-project debian-cloud --metadata startup-script-url=gs://$1/startup_install_java8.sh

# wait for VM startup-script to be executed
echo "Waiting for VM to be ready"
while [ "$(gcloud compute instances list --filter='name:debian9-java8-img AND status:terminated' --format json)" = "[]" ]; do
  echo "VM is not ready yet. Check in 5 seconds..."
  sleep 5
done
echo "VM is ready."

# generate custom image
echo "Executing gcloud compute images create debian9-java8-img --source-disk debian9-java8-img --source-disk-zone us-central1-f"
gcloud compute images create debian9-java8-img --source-disk debian9-java8-img --source-disk-zone us-central1-f

# delete temporary VM
echo "Executing gcloud compute instances delete debian9-java8-img"
gcloud compute instances delete debian9-java8-img
