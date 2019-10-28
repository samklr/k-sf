/**
 * Copyright (C) 2019 Walid HAOUARI (walid.haouari@jobteaser.com)
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

import com.google.api.client.http.GenericUrl;
import com.sk.kafka.connect.salesforce.rest.model.AuthenticationResponse;
import org.apache.kafka.connect.errors.ConnectException;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BayeuxHelper {
    private static Logger log = LoggerFactory.getLogger(SalesforceSourceConnector.class);

    public static BayeuxClient createClient(final GenericUrl streamingUrl,
                                            final long connectTimeout,
                                            final AuthenticationResponse authResponse) {

        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        httpClient.setConnectTimeout(connectTimeout);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new ConnectException("Exception thrown while starting httpClient.", e);
        }

        Map<String, Object> options = new HashMap<>();

        LongPollingTransport transport = new LongPollingTransport(options, httpClient) {

            @Override
            protected void customize(Request request) {
                super.customize(request);
                String headerValue = String.format("Authorization: %s %s",
                        authResponse.tokenType(),
                        authResponse.accessToken());

                request.header("Authorization", headerValue);
            }
        };

        return new BayeuxClient(streamingUrl.toString(), transport);
    }
}
