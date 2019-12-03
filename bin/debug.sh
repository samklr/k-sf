#!/usr/bin/env bash

mvn clean package

export CLASSPATH="$(find `pwd`/target/kafka-connect-salesforce-1.0-SNAPSHOT-package/share/java/ -type f -name '*.jar' | tr '\n' ':')"


$CONFLUENT_HOME/bin/connect-standalone connect/connect-avro-docker.properties config/salesforce.activity.properties