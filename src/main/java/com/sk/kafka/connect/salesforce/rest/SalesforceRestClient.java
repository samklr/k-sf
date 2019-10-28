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
package com.sk.kafka.connect.salesforce.rest;

import com.sk.kafka.connect.salesforce.rest.model.ApiVersion;
import com.sk.kafka.connect.salesforce.rest.model.AuthenticationResponse;
import com.sk.kafka.connect.salesforce.rest.model.PushTopic;
import com.sk.kafka.connect.salesforce.rest.model.SObjectDescriptor;
import com.sk.kafka.connect.salesforce.rest.model.SObjectMetadata;
import com.sk.kafka.connect.salesforce.rest.model.SObjectsResponse;

import java.util.List;

public interface SalesforceRestClient {
    AuthenticationResponse authenticate();

    void setApiVersion(final String version);

    List<ApiVersion> listApiVersions();

    SObjectsResponse listObjects();

    SObjectMetadata getSObjectMetadata(final String sfObjectName);

    SObjectDescriptor getSObjectDescriptor(SObjectMetadata metadata);

    List<PushTopic> listPushTopics();

    PushTopic getPushTopic(final String sfPushTopicName);

    void createPushTopic(PushTopic pushTopic);

    void shutdown();
}
