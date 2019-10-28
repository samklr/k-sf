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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.sk.kafka.connect.salesforce.rest.SalesforceRestClient;
import com.sk.kafka.connect.salesforce.rest.SalesforceRestClientFactory;
import com.sk.kafka.connect.salesforce.rest.model.AuthenticationResponse;
import com.sk.kafka.connect.salesforce.rest.model.SObjectDescriptor;
import com.sk.kafka.connect.salesforce.rest.model.SObjectMetadata;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;


public class SalesforceSourceTask extends SourceTask implements ClientSessionChannel.MessageListener {
    static final Long FROM_EARLIEST = -2L;
    static final Long FROM_LATEST = -1L;
    static final Logger log = LoggerFactory.getLogger(SalesforceSourceTask.class);
    final ConcurrentLinkedDeque<SourceRecord> messageQueue = new ConcurrentLinkedDeque<>();
    SalesforceSourceConfig config;
    SalesforceRestClient salesforceRestClient;
    AuthenticationResponse authenticationResponse;
    SObjectDescriptor descriptor;
    SObjectMetadata metadata;
    GenericUrl streamingUrl;
    BayeuxClient streamingClient;
    Schema keySchema;
    Schema valueSchema;
    String channel;
    Map<String, String> sourcePartition = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentMap<String, Long> offsets = new ConcurrentHashMap<>();

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }

    @Override
    public void start(Map<String, String> map) {
        this.config = new SalesforceSourceConfig(map);
        this.salesforceRestClient = SalesforceRestClientFactory.create(this.config);

        log.info("Authenticating...");
        this.authenticationResponse = this.salesforceRestClient.authenticate();

        salesforceRestClient.setApiVersion(this.config.version());

        log.info("Looking for metadata for {}", this.config.salesForceObject());
        this.metadata = salesforceRestClient.getSObjectMetadata(this.config.salesForceObject());
        Preconditions.checkNotNull(this.metadata, "Could not find metadata for '%s'", this.config.salesForceObject());

        log.info("Looking for SOObjectDescriptor for {}", this.config.salesForceObject());
        this.descriptor = salesforceRestClient.getSObjectDescriptor(this.metadata);
        //2013-05-06T00:00:00+00:00
        Preconditions.checkNotNull(this.descriptor, "Could not find descriptor for '%s'", this.config.salesForceObject());

        this.keySchema = SObjectHelper.keySchema(this.descriptor);
        this.valueSchema = SObjectHelper.valueSchema(this.descriptor, this.config.ignoreTextAreaFields(), this.config.includeEventMetadata());

        this.streamingUrl = new GenericUrl(this.authenticationResponse.instance_url());
        this.streamingUrl.setRawPath(String.format("/cometd/%s", this.config.version()));
        log.info("Streaming url configured to {}", this.streamingUrl);

        this.channel = String.format("/topic/%s", this.config.salesForcePushTopicName());

        this.sourcePartition.put("pushtopic", this.config.salesForcePushTopicName());

        Long offset = loadOffset();
        log.info("Starting from offset : " + offset);

        this.streamingClient = BayeuxHelper.createClient(this.streamingUrl,
                                                         this.config.connectTimeout(),
                                                         this.authenticationResponse);

        simpleHandshake(this.config.bayeuxClientTimeout(), offset);
       // addMetaListeners(); TODO FIX THIS
        subscribe(this.channel, this);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> records = new ArrayList<>(this.config.pollBulkSize());
        long pollIdleTime = this.config.pollIdleTime();

        while (records.isEmpty()) {
            int size = messageQueue.size();

            for (int i = 0; i < size; i++) {
                SourceRecord record = this.messageQueue.poll();

                if (null == record) {
                    break;
                }

                records.add(record);
            }

            if (records.isEmpty()) {
                Thread.sleep(pollIdleTime);
            }
        }

        return records;
    }

    @Override
    public void stop() {
        this.streamingClient.disconnect();
        this.salesforceRestClient.shutdown();
    }

    @Override
    public void onMessage(ClientSessionChannel clientSessionChannel, Message message) {

        // TODO : Extract in a MessageReader
        try {
            String jsonMessage = message.getJSON();
            log.info("message={}", jsonMessage);

            JsonNode jsonNode = objectMapper.readTree(jsonMessage);
            SourceRecord record = SObjectHelper.convert(jsonNode,
                                                        this.sourcePartition,
                                                        this.config.salesForcePushTopicName(),
                                                        this.config.kafkaTopic(),
                                                        keySchema,
                                                        valueSchema,
                                                        this.config.includeEventMetadata());

            this.messageQueue.add(record);

        } catch (Exception ex) {
            log.error("Exception thrown while processing message.", ex);
        }
    }


    private Long loadOffset() {

        Map<String, Object> offsetMap = context.offsetStorageReader().offset(this.sourcePartition);

        if (offsetMap == null) {
            return FROM_LATEST;
        }

        return (Long) offsetMap.get(this.config.salesForcePushTopicName());
    }

    private void simpleHandshake(final long bayeuxClientTimeout, final long offset) {

        this.streamingClient.getChannel(Channel.META_HANDSHAKE).addListener(new ClientSessionChannel.MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel clientSessionChannel, Message message) {

                if (!message.isSuccessful()) {
                    log.error("Error during handshake: {} {}", message.get("error"), message.get("exception"));
                } else {
                    log.info("onMessage - {}", message);
                    offsets.clear();
                    offsets.put(channel, offset);
                    log.info("Positionning offset for {} at {}", channel, offset);

                }
            }
        });

        this.streamingClient.addExtension(new ReplayExtension(offsets));
        log.info("Starting handshake");
        this.streamingClient.handshake();

        if (!this.streamingClient.waitFor(bayeuxClientTimeout, BayeuxClient.State.CONNECTED)) {
            throw new ConnectException("Not connected after " + bayeuxClientTimeout + " ms.");
        }
    }

    private void subscribe(final String channel,
                           ClientSessionChannel.MessageListener msgListener) {

        log.info("Subscribing to {}", channel);
        this.streamingClient.getChannel(channel).subscribe(msgListener);
        log.info("subscribed to session {}", this.streamingClient.getChannel(channel).getSession());
    }

    /*private void addMetaListeners() {
        SimpleMsgListener simpleMsgListener = new SimpleMsgListener();
        this.streamingClient.getChannel(Channel.META_CONNECT).addListener(simpleMsgListener);
        this.streamingClient.getChannel(Channel.META_DISCONNECT).addListener(simpleMsgListener);
        this.streamingClient.getChannel(Channel.META_UNSUBSCRIBE).addListener(simpleMsgListener);
    }*/
}