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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSession.Extension.Adapter;

public class ReplayExtension extends Adapter {
    private static final String EXTENSION_NAME = "replay";
    private final ConcurrentMap<String, Long> dataMap;
    private final AtomicBoolean supported = new AtomicBoolean();

    public ReplayExtension(ConcurrentMap<String, Long> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public boolean rcv(ClientSession session, Message.Mutable message) {
        Object data = message.get(EXTENSION_NAME);
        if (this.supported.get() && data != null) {
            try {
                dataMap.put(message.getChannel(), (Long) data);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean rcvMeta(ClientSession session, Message.Mutable message) {
        switch (message.getChannel()) {
            case Channel.META_HANDSHAKE:
                Map<String, Object> ext = message.getExt(false);
                this.supported.set(ext != null && Boolean.TRUE.equals(ext.get(EXTENSION_NAME)));
        }
        return true;
    }

    @Override
    public boolean sendMeta(ClientSession session, Message.Mutable message) {
        switch (message.getChannel()) {
            case Channel.META_HANDSHAKE:
                message.getExt(true).put(EXTENSION_NAME, Boolean.TRUE);
                break;
            case Channel.META_SUBSCRIBE:
                if (supported.get()) {
                    message.getExt(true).put(EXTENSION_NAME, dataMap);
                }
                break;
        }
        return true;
    }
}