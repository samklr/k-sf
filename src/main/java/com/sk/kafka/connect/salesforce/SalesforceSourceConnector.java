/**
 *
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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.sk.kafka.connect.salesforce.rest.SalesforceRestClient;
import com.sk.kafka.connect.salesforce.rest.SalesforceRestClientFactory;
import com.sk.kafka.connect.salesforce.rest.model.PushTopic;
import com.sk.kafka.connect.salesforce.rest.model.SObjectDescriptor;
import com.sk.kafka.connect.salesforce.rest.model.SObjectMetadata;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class SalesforceSourceConnector extends SourceConnector {
    private static Logger log = LoggerFactory.getLogger(SalesforceSourceConnector.class);
    List<Map<String, String>> configs = new ArrayList<>();
    private SalesforceSourceConfig config;

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }

    @Override
    public void start(Map<String, String> map) {
        config = new SalesforceSourceConfig(map);

        SalesforceRestClient client = SalesforceRestClientFactory.create(this.config);
        client.authenticate();

        client.setApiVersion(this.config.version());

        SObjectMetadata sObjectMetadata = client.getSObjectMetadata(this.config.salesForceObject());

        //2013-05-06T00:00:00+00:00
        Preconditions.checkNotNull(sObjectMetadata, "Could not find metadata for '%s'", this.config.salesForceObject());

        SObjectDescriptor sObjectDescriptor = client.getSObjectDescriptor(sObjectMetadata);
        Preconditions.checkNotNull(sObjectDescriptor, "Could not find descriptor for object '%s'", this.config.salesForceObject());

        PushTopic pushTopic = getOrCreatePushTopic(client, sObjectDescriptor);
        Preconditions.checkNotNull(pushTopic, "Cannot get or create PushTopic '%s'", this.config.salesForcePushTopicName());

        Map<String, String> taskSettings = new HashMap<>();
        taskSettings.putAll(map);
        taskSettings.put(SalesforceSourceConfig.VERSION_CONF, this.config.version());
        this.configs.add(taskSettings);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return SalesforceSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        return this.configs;
    }

    @Override
    public void stop() {
    }

    @Override
    public ConfigDef config() {
        return SalesforceSourceConfig.conf();
    }

    private String forgeGenericQuery(Set<String> fields, String pushTopicName) {
        return String.format("SELECT %s FROM %s", Joiner.on(',').join(fields), pushTopicName);
    }

    private PushTopic getOrCreatePushTopic(SalesforceRestClient client, SObjectDescriptor sObjectDescriptor) {

        // TODO : Extract in a PushTopicHelper and change the internal behaviour
        // TODO Add listeners and callbacks around SF Pushtopics ==>> proper logging

        PushTopic pushTopic = client.getPushTopic(this.config.salesForcePushTopicName());

        if (null == pushTopic && this.config.salesForcePushTopicCreate()) {

            log.warn("PushTopic {} was not found. Creating it...", this.config.salesForcePushTopicName());

            String configQuery = this.config.salesForcePushTopicQuery();
            Set<String> fields = SObjectHelper.getFieldNames(sObjectDescriptor, this.config.ignoreTextAreaFields());
            String query = configQuery.isEmpty() ? forgeGenericQuery(fields, sObjectDescriptor.name()) : configQuery;

            log.info("Forging PushTopic {}", this.config.salesForcePushTopicName());
            pushTopic = new PushTopic(this.config, query);

            log.info("Creating PushTopic {}", pushTopic.name());
            client.createPushTopic(pushTopic);

            log.info("PushTopic {} created", pushTopic.name());
        }

        return pushTopic;
    }
}
