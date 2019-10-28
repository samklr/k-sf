/**
 * Connect Salesforce
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sk.kafka.connect.salesforce;

import io.confluent.kafka.connect.utils.config.ValidPattern;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.Map;


public class SalesforceSourceConfig extends AbstractConfig {

    public static final String USERNAME_CONF = "salesforce.username";
    public static final String PASSWORD_CONF = "salesforce.password";
    public static final String PASSWORD_TOKEN_CONF = "salesforce.password.token";
    public static final String CONSUMER_KEY_CONF = "salesforce.consumer.key";
    public static final String CONSUMER_SECRET_CONF = "salesforce.consumer.secret";
    public static final String INSTANCE_CONF = "salesforce.instance";
    public static final String SALESFORCE_PUSH_TOPIC_NAME_CONF = "salesforce.push.topic.name";
    public static final String SALESFORCE_PUSH_TOPIC_CREATE_CONF = "salesforce.push.topic.create";
    public static final String SALESFORCE_PUSH_TOPIC_QUERY_CONF = "salesforce.push.topic.query";
    public static final String SALESFORCE_PUSH_TOPIC_NOTIFY_FOR_FIELDS_CONF = "salesforce.push.topic.notify.fields";
    public static final String SALESFORCE_PUSH_TOPIC_NOTIFY_CREATE_CONF = "salesforce.push.topic.notify.create";
    public static final String SALESFORCE_PUSH_TOPIC_NOTIFY_UPDATE_CONF = "salesforce.push.topic.notify.update";
    public static final String SALESFORCE_PUSH_TOPIC_NOTIFY_DELETE_CONF = "salesforce.push.topic.notify.delete";
    public static final String SALESFORCE_PUSH_TOPIC_NOTIFY_UNDELETE_CONF = "salesforce.push.topic.notify.undelete";
    public static final String SALESFORCE_OBJECT_CONF = "salesforce.object";
    public static final String VERSION_CONF = "salesforce.version";

    public static final String KAFKA_TOPIC_CONF = "kafka.topic";
    public static final String CURL_LOGGING_CONF = "curl.logging";

    public static final String CONNECTION_TIMEOUT_CONF = "connection.timeout";
    public static final String BAYEUX_CLIENT_TIMEOUT_CONF = "bayeux.client.timeout";
    public static final String TASK_POLL_RECORD_BULK_SIZE_CONF = "poll.record.bulk.size";
    public static final String TASK_POLL_IDLE_TIME_CONF = "poll.idle.time";
    public static final String IGNORE_TEXT_AREA_FIELDS_CONF = "ignore.text.area.fields";
    public static final String INCLUDE_EVENT_METADATA_CONF = "include.event.metadata";

    static final String VERSION_DOC = "The version of the salesforce API to use.";
    static final String USERNAME_DOC = "Salesforce username to connect with.";
    static final String PASSWORD_DOC = "Salesforce password to connect with.";
    static final String PASSWORD_TOKEN_DOC = "The Salesforce security token associated with the username.";
    static final String CONSUMER_KEY_DOC = "The consumer key for the OAuth application.";
    static final String CONSUMER_SECRET_DOC = "The consumer secret for the OAuth application.";
    static final String INSTANCE_DOC = "The Salesforce instance to connect to.";
    static final String CURL_LOGGING_DOC = "If enabled the logs will output the equivalent curl commands. This is a security risk because your authorization header will end up in the log file. Use at your own risk.";
    static final String CONNECTION_TIMEOUT_DOC = "The amount of time to wait while connecting to the Salesforce streaming endpoint.";
    static final String BAYEUX_CLIENT_TIMEOUT_DOC = "The amount of time(in ms) to wait while Handshaking to the Salesforce streaming channel.";
    static final String TASK_POLL_RECORD_BULK_SIZE_DOC = "The size of the bulk of records to process.";
    static final String TASK_POLL_IDLE_TIME_DOC = "The amount of time(in ms) to wait before next poll.";
    static final String IGNORE_TEXT_AREA_FIELDS_DOC = "Ignoring fields typed 'TextArea' because the Salesforce REST API does not support this type yet.";
    static final String INCLUDE_EVENT_METADATA_DOC = "Include Event Metadata(createdAt, type)";
    static final String SALESFORCE_PUSH_TOPIC_NAME_DOC = "The Salesforce topic to subscribe to. If " + SALESFORCE_PUSH_TOPIC_CREATE_CONF +
            " is set to true, a PushTopic with this name will be created.";
    static final String SALESFORCE_PUSH_TOPIC_CREATE_DOC = "Flag to determine if the PushTopic should be created if it does not exist.";
    static final String SALESFORCE_PUSH_TOPIC_QUERY_DOC = "The SOQL query statement that determines which record changes trigger events to be sent to the channel.";
    static final String SALESFORCE_PUSH_TOPIC_NOTIFY_FOR_FIELDS_DOC = "The Property(All, Referenced (default), Select, Where) that specifies which fields are evaluated to generate a notification.";
    static final String SALESFORCE_PUSH_TOPIC_NOTIFY_CREATE_DOC = "Flag to determine if the PushTopic should respond to creates.";
    static final String SALESFORCE_PUSH_TOPIC_NOTIFY_UPDATE_DOC = "Flag to determine if the PushTopic should respond to updates.";
    static final String SALESFORCE_PUSH_TOPIC_NOTIFY_DELETE_DOC = "Flag to determine if the PushTopic should respond to deletes.";
    static final String SALESFORCE_PUSH_TOPIC_NOTIFY_UNDELETE_DOC = "Flag to determine if the PushTopic should respond to undeletes.";
    static final String SALESFORCE_OBJECT_DOC = "The Salesforce object to create a topic for.";
    static final String KAFKA_TOPIC_DOC = "The Kafka topic to write the SalesForce data to.";

    public SalesforceSourceConfig(ConfigDef config, Map<String, ?> parsedConfig) {
        super(config, parsedConfig);
    }

    public SalesforceSourceConfig(Map<String, ?> parsedConfig) {
        this(conf(), parsedConfig);
    }

    public static ConfigDef conf() {
        return new ConfigDef()
                .define(USERNAME_CONF, Type.STRING, Importance.HIGH, USERNAME_DOC)
                .define(PASSWORD_CONF, Type.PASSWORD, Importance.HIGH, PASSWORD_DOC)
                .define(PASSWORD_TOKEN_CONF, Type.PASSWORD, Importance.HIGH, PASSWORD_TOKEN_DOC)
                .define(CONSUMER_KEY_CONF, Type.STRING, Importance.HIGH, CONSUMER_KEY_DOC)
                .define(CONSUMER_SECRET_CONF, Type.PASSWORD, Importance.HIGH, CONSUMER_SECRET_DOC)
                .define(INSTANCE_CONF, Type.STRING, "", Importance.HIGH, INSTANCE_DOC)
                .define(CURL_LOGGING_CONF, Type.BOOLEAN, false, Importance.LOW, CURL_LOGGING_DOC)
                .define(SALESFORCE_OBJECT_CONF, Type.STRING, Importance.HIGH, SALESFORCE_OBJECT_DOC)
                .define(KAFKA_TOPIC_CONF, Type.STRING, Importance.HIGH, KAFKA_TOPIC_DOC)
                .define(CONNECTION_TIMEOUT_CONF, Type.LONG, 30000L, Importance.LOW, CONNECTION_TIMEOUT_DOC)
                .define(BAYEUX_CLIENT_TIMEOUT_CONF, Type.LONG, 30000L, Importance.LOW, BAYEUX_CLIENT_TIMEOUT_DOC)
                .define(TASK_POLL_RECORD_BULK_SIZE_CONF, Type.INT, 256, Importance.LOW, TASK_POLL_RECORD_BULK_SIZE_DOC)
                .define(TASK_POLL_IDLE_TIME_CONF, Type.LONG, 100L, Importance.LOW, TASK_POLL_IDLE_TIME_DOC)
                .define(IGNORE_TEXT_AREA_FIELDS_CONF, Type.BOOLEAN, true, Importance.LOW, IGNORE_TEXT_AREA_FIELDS_DOC)
                .define(INCLUDE_EVENT_METADATA_CONF, Type.BOOLEAN, true, Importance.LOW, INCLUDE_EVENT_METADATA_DOC)
                .define(VERSION_CONF, Type.STRING, "latest", ValidPattern.of("^(latest|[\\d\\.]+)$"), Importance.LOW, VERSION_DOC)
                .define(SALESFORCE_PUSH_TOPIC_NAME_CONF, Type.STRING, Importance.HIGH, SALESFORCE_PUSH_TOPIC_NAME_DOC)
                .define(SALESFORCE_PUSH_TOPIC_CREATE_CONF, Type.BOOLEAN, true, Importance.LOW, SALESFORCE_PUSH_TOPIC_CREATE_DOC)
                .define(SALESFORCE_PUSH_TOPIC_QUERY_CONF, Type.STRING, "", Importance.MEDIUM, SALESFORCE_PUSH_TOPIC_QUERY_DOC)
                .define(SALESFORCE_PUSH_TOPIC_NOTIFY_FOR_FIELDS_CONF, Type.STRING, "Referenced", ValidPattern.of("^(All|Referenced|Select|Where)$"), Importance.MEDIUM, SALESFORCE_PUSH_TOPIC_NOTIFY_FOR_FIELDS_DOC)
                .define(SALESFORCE_PUSH_TOPIC_NOTIFY_CREATE_CONF, Type.BOOLEAN, true, Importance.LOW, SALESFORCE_PUSH_TOPIC_NOTIFY_CREATE_DOC)
                .define(SALESFORCE_PUSH_TOPIC_NOTIFY_UPDATE_CONF, Type.BOOLEAN, true, Importance.LOW, SALESFORCE_PUSH_TOPIC_NOTIFY_UPDATE_DOC)
                .define(SALESFORCE_PUSH_TOPIC_NOTIFY_DELETE_CONF, Type.BOOLEAN, true, Importance.LOW, SALESFORCE_PUSH_TOPIC_NOTIFY_DELETE_DOC)
                .define(SALESFORCE_PUSH_TOPIC_NOTIFY_UNDELETE_CONF, Type.BOOLEAN, true, Importance.LOW, SALESFORCE_PUSH_TOPIC_NOTIFY_UNDELETE_DOC);
    }

    public String username() {
        return this.getString(USERNAME_CONF);
    }

    public String password() {
        return this.getPassword(PASSWORD_CONF).value();
    }

    public String passwordToken() {
        return this.getPassword(PASSWORD_TOKEN_CONF).value();
    }

    public String consumerKey() {
        return this.getString(CONSUMER_KEY_CONF);
    }

    public String consumerSecret() {
        return this.getPassword(CONSUMER_SECRET_CONF).value();
    }

    public String instance() {
        return this.getString(INSTANCE_CONF);
    }

    public boolean curlLogging() {
        return this.getBoolean(CURL_LOGGING_CONF);
    }

    public String kafkaTopic() {
        return this.getString(KAFKA_TOPIC_CONF);
    }

    public String salesForcePushTopicName() {
        return this.getString(SALESFORCE_PUSH_TOPIC_NAME_CONF);
    }

    public boolean salesForcePushTopicCreate() {
        return this.getBoolean(SALESFORCE_PUSH_TOPIC_CREATE_CONF);
    }

    public String salesForcePushTopicQuery() {
        return this.getString(SALESFORCE_PUSH_TOPIC_QUERY_CONF);
    }

    public String salesForcePushTopicNotifyForFields() {
        return this.getString(SALESFORCE_PUSH_TOPIC_NOTIFY_FOR_FIELDS_CONF);
    }

    public String salesForceObject() {
        return this.getString(SALESFORCE_OBJECT_CONF);
    }

    public long connectTimeout() {
        return this.getLong(CONNECTION_TIMEOUT_CONF);
    }

    public long bayeuxClientTimeout() {
        return this.getLong(BAYEUX_CLIENT_TIMEOUT_CONF);
    }

    public int pollBulkSize() {
        return this.getInt(TASK_POLL_RECORD_BULK_SIZE_CONF);
    }

    public long pollIdleTime() {
        return this.getLong(TASK_POLL_IDLE_TIME_CONF);
    }

    public boolean ignoreTextAreaFields() {
        return this.getBoolean(IGNORE_TEXT_AREA_FIELDS_CONF);
    }

    public boolean includeEventMetadata() {
        return this.getBoolean(INCLUDE_EVENT_METADATA_CONF);
    }

    public boolean salesForcePushTopicNotifyCreate() {
        return this.getBoolean(SALESFORCE_PUSH_TOPIC_NOTIFY_CREATE_CONF);
    }

    public boolean salesForcePushTopicNotifyUpdate() {
        return this.getBoolean(SALESFORCE_PUSH_TOPIC_NOTIFY_UPDATE_CONF);
    }

    public boolean salesForcePushTopicNotifyDelete() {
        return this.getBoolean(SALESFORCE_PUSH_TOPIC_NOTIFY_DELETE_CONF);
    }

    public boolean salesForcePushTopicNotifyUndelete() {
        return this.getBoolean(SALESFORCE_PUSH_TOPIC_NOTIFY_UNDELETE_CONF);
    }

    public String version() {
        return this.getString(VERSION_CONF);
    }
}
