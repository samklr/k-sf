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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.sk.kafka.connect.salesforce.rest.model.SObjectDescriptor;
import io.confluent.kafka.connect.utils.data.Parser;
import io.confluent.kafka.connect.utils.data.type.DateTypeParser;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TimeZone;


class SObjectHelper {
    static final Parser PARSER;
    private static final Logger log = LoggerFactory.getLogger(SObjectHelper.class);
    private static final String EVENT_TYPE_FIELD_NAME = "_EventType";
    private static final String EVENT_DATE_FIELD_NAME = "_EventDate";

    static {
        Parser p = new Parser();
        //"2016-08-15T22:07:59.000Z"
        p.registerTypeParser(Timestamp.SCHEMA, new DateTypeParser(TimeZone.getTimeZone("UTC"), new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'")));
        PARSER = p;
    }

    public static boolean isTextArea(SObjectDescriptor.Field field) {
        return "textarea".equalsIgnoreCase(field.type());
    }

    public static Schema schema(SObjectDescriptor.Field field) {
        SchemaBuilder builder;

        boolean optional = true;

        switch (field.type()) {
            case "id":
                optional = false;
                builder = SchemaBuilder.string().doc("Unique identifier for the object.");
                break;
            case "address":
                builder = SchemaBuilder.string();
                break;
            case "anytype":
                builder = SchemaBuilder.string();
                break;
            case "blob":
                builder = SchemaBuilder.bytes();
                break;
            case "boolean":
                builder = SchemaBuilder.bool();
                break;
            case "calculated":
                builder = SchemaBuilder.string();
                break;
            case "combobox":
                builder = SchemaBuilder.string();
                break;
            case "currency":
                builder = SchemaBuilder.string();
                break;
            case "datacategorygroupreference":
                builder = SchemaBuilder.string();
                break;
            case "date":
                builder = Date.builder();
                break;
            case "datetime":
                builder = Timestamp.builder();
                break;
            case "decimal":
                builder = Decimal.builder(field.scale());
                break;
            case "double":
                builder = SchemaBuilder.float64();
                break;
            case "email":
                builder = SchemaBuilder.string();
                break;
            case "encryptedstring":
                builder = SchemaBuilder.string();
                break;
            case "int":
                builder = SchemaBuilder.int32();
                break;
            case "location":
                builder = SchemaBuilder.string();
                break;
            case "multipicklist":
                builder = SchemaBuilder.string();
                break;
            case "percent":
                builder = SchemaBuilder.string();
                break;
            case "picklist":
                builder = SchemaBuilder.string();
                break;
            case "phone":
                builder = SchemaBuilder.string();
                break;
            case "reference":
                builder = SchemaBuilder.string();
                break;
            case "string":
                builder = SchemaBuilder.string();
                break;
            case "textarea":
                builder = SchemaBuilder.string();
                break;
            case "url":
                builder = SchemaBuilder.string();
                break;
            default:
                log.warn("Explicit '%s' type '%s' mapping not found, casting to String", field.name(), field.type());
                builder = SchemaBuilder.string();
        }

        if (optional) {
            builder = builder.optional();
        }

        return builder.build();
    }

    // TODO : Extract in a SchemaHandler
    public static Schema valueSchema(SObjectDescriptor descriptor, boolean ignoreTextArea, boolean includeEventMetaData) {
        String name = String.format("%s.%s", SObjectHelper.class.getPackage().getName(), descriptor.name());
        SchemaBuilder builder = SchemaBuilder.struct();
        builder.name(name);

        for (SObjectDescriptor.Field field : descriptor.fields()) {
            if (!ignoreTextArea || !isTextArea(field)) {
                Schema schema = schema(field);
                builder.field(field.name(), schema);
            }
        }

        if (includeEventMetaData) {
            builder.field(EVENT_TYPE_FIELD_NAME, SchemaBuilder.STRING_SCHEMA);
            builder.field(EVENT_DATE_FIELD_NAME, SchemaBuilder.STRING_SCHEMA);
        }

        return builder.build();
    }

    public static Schema keySchema(SObjectDescriptor descriptor) {
        String name = String.format("%s.%sKey", SObjectHelper.class.getPackage().getName(), descriptor.name());
        SchemaBuilder builder = SchemaBuilder.struct();
        builder.name(name);

        SObjectDescriptor.Field keyField = null;

        for (SObjectDescriptor.Field field : descriptor.fields()) {
            if ("id".equalsIgnoreCase(field.type())) {
                keyField = field;
                break;
            }
        }

        if (null == keyField) {
            throw new IllegalStateException("Could not find an id field for " + descriptor.name());
        }

        Schema keySchema = schema(keyField);
        builder.field(keyField.name(), keySchema);
        return builder.build();
    }

    // TODO : Extract in a converter
    public static void convertStruct(JsonNode dataNode, Schema schema, Struct struct, boolean includeEventMetaData) {
        JsonNode eventNode = dataNode.get("event");
        JsonNode sobjectNode = dataNode.get("sobject");

        if (includeEventMetaData) {
            ((ObjectNode) sobjectNode).put(EVENT_TYPE_FIELD_NAME, eventNode.get("type").asText());
            ((ObjectNode) sobjectNode).put(EVENT_DATE_FIELD_NAME, eventNode.get("createdDate").asText());
        }

        for (Field field : schema.fields()) {
            String fieldName = field.name();
            JsonNode valueNode = sobjectNode.findValue(fieldName);
            Object value = PARSER.parseJsonNode(field.schema(), valueNode);
            struct.put(field, value);
        }
    }

    public static SourceRecord convert(JsonNode jsonNode,
                                       Map<String, ?> sourcePartition,
                                       String pushTopicName,
                                       String topic,
                                       Schema keySchema,
                                       Schema valueSchema,
                                       boolean includeEventMetaData) {
        Preconditions.checkNotNull(jsonNode);
        Preconditions.checkState(jsonNode.isObject());
        JsonNode dataNode = jsonNode.get("data");
        JsonNode eventNode = dataNode.get("event");

        long replayId = eventNode.get("replayId").asLong();
        Struct keyStruct = new Struct(keySchema);
        Struct valueStruct = new Struct(valueSchema);
        convertStruct(dataNode, keySchema, keyStruct, includeEventMetaData);
        convertStruct(dataNode, valueSchema, valueStruct, includeEventMetaData);
        Map<String, Long> sourceOffset = ImmutableMap.of(pushTopicName, replayId);

        return new SourceRecord(sourcePartition, sourceOffset, topic, keySchema, keyStruct, valueSchema, valueStruct);
    }

    public static LinkedHashSet<String> getFieldNames(SObjectDescriptor sObjectDescriptor, boolean ignoreTextArea) {
        LinkedHashSet<String> fields = new LinkedHashSet<>();
        for (SObjectDescriptor.Field f : sObjectDescriptor.fields()) {
            if (!ignoreTextArea || !isTextArea(f)) {
                fields.add(f.name());
            }
        }

        return fields;
    }

}
