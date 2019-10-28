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
import com.sk.kafka.connect.salesforce.rest.model.SObjectDescriptor;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class SObjectHelperTest {

    private static final boolean IGNORE_TEXT_AREA = true;
    private static final boolean INCLUDE_EVENT_METADATA = true;

    @Test
    public void valueSchema() {
        final SObjectDescriptor descriptor = TestData.sObjectDescriptor();
        final Schema schema = SObjectHelper.valueSchema(descriptor, IGNORE_TEXT_AREA, INCLUDE_EVENT_METADATA);

        assertNotNull(schema);
        assertEquals("com.sk.kafka.connect.salesforce.Lead", schema.name());
    }

    @Test
    public void convertStruct() {
        final SObjectDescriptor descriptor = TestData.sObjectDescriptor();
        JsonNode jsonNode = TestData.salesForceEvent();
        JsonNode dataNode = jsonNode.get("data");

        final Schema schema = SObjectHelper.valueSchema(descriptor, IGNORE_TEXT_AREA, INCLUDE_EVENT_METADATA);
        Struct struct = new Struct(schema);
        SObjectHelper.convertStruct(dataNode, schema, struct, INCLUDE_EVENT_METADATA);

        assertNotNull(struct);
    }

}
