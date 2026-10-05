/*
 *   Copyright 2024 RETIT GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.retit.opentelemetry.javaagent.extension.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.retit.opentelemetry.javaagent.extension.commons.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MetricPublishingServiceTest {

    // attributes of a typical server span created by the tomcat instrumentation
    private static final Attributes SERVER_SPAN_ATTRIBUTES = Attributes.builder()
            .put("http.request.method", "GET")
            .put("http.route", "/app/**")
            .put("http.response.status_code", 200L)
            .put("url.path", "/app/exampleTransaction")
            .put("url.scheme", "https")
            .put("url.query", "person=example-person-id&page=3&conversationId=example-conversation-id")
            .put("server.address", "localhost")
            .put("client.address", "192.0.2.10")
            .put("network.peer.address", "127.0.0.6")
            .put("network.peer.port", 40853L)
            .put("network.protocol.version", "1.1")
            .put("user_agent.original", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) HeadlessChrome/151.0.0.0")
            .put("thread.id", 124L)
            .put("thread.name", "http-nio-8080-exec-1")
            .put("io.retit.startcputime", 23424589223L)
            .put("io.retit.endcputime", 23490117509L)
            .build();

    @BeforeEach
    @AfterEach
    public void clearProperties() {
        System.clearProperty(Constants.RETIT_METRICS_EXCLUDED_ATTRIBUTES_CONFIGURATION_PROPERTY);
    }

    @Test
    public void testHighCardinalityAttributesAreExcludedByDefault() {
        Attributes filteredAttributes = MetricPublishingService.getAttributesWithoutExcludedAttributes(SERVER_SPAN_ATTRIBUTES);

        Assertions.assertEquals(Attributes.builder()
                .put("http.request.method", "GET")
                .put("http.route", "/app/**")
                .put("http.response.status_code", 200L)
                .put("url.path", "/app/exampleTransaction")
                .put("url.scheme", "https")
                .put("server.address", "localhost")
                .build(), filteredAttributes);
    }

    // one attribute per default excluded prefix, the names are spelled out on purpose
    // so that a misspelled or removed prefix in the default list fails this test
    @ParameterizedTest
    @ValueSource(strings = {
            "io.retit.startcputime",
            "network.peer.address",
            "thread.name",
            "user.id",
            "user_agent.original",
            "client.address",
            "instance.id",
            "url.query",
            "url.full",
            "url.fragment",
            "http.request.header.cookie",
            "http.response.header.set-cookie",
            "session.id",
            "enduser.id",
            "messaging.message.id",
            "messaging.message.conversation_id",
            "db.statement",
            "db.query.text"
    })
    public void testDefaultExcludedAttributeIsNotPublished(final String attributeName) {
        Attributes spanAttributes = Attributes.builder()
                .put("http.request.method", "GET")
                .put(attributeName, "example-value")
                .build();

        Attributes filteredAttributes = MetricPublishingService.getAttributesWithoutExcludedAttributes(spanAttributes);

        Assertions.assertEquals(Attributes.of(AttributeKey.stringKey("http.request.method"), "GET"), filteredAttributes);
    }

    // attributes used to group transactions must not be caught by an overly broad default prefix
    @ParameterizedTest
    @ValueSource(strings = {
            "http.request.method",
            "http.route",
            "url.path",
            "url.scheme",
            "server.address",
            "messaging.destination.name",
            "db.system"
    })
    public void testTransactionAttributeIsPublished(final String attributeName) {
        Attributes spanAttributes = Attributes.of(AttributeKey.stringKey(attributeName), "example-value");

        Attributes filteredAttributes = MetricPublishingService.getAttributesWithoutExcludedAttributes(spanAttributes);

        Assertions.assertEquals(spanAttributes, filteredAttributes);
    }

    @Test
    public void testConfiguredAttributesReplaceDefaultList() {
        System.setProperty(Constants.RETIT_METRICS_EXCLUDED_ATTRIBUTES_CONFIGURATION_PROPERTY, " network, thread, user,client ,");

        Attributes filteredAttributes = MetricPublishingService.getAttributesWithoutExcludedAttributes(SERVER_SPAN_ATTRIBUTES);

        Assertions.assertEquals(Attributes.builder()
                .put("http.request.method", "GET")
                .put("http.route", "/app/**")
                .put("http.response.status_code", 200L)
                .put("url.path", "/app/exampleTransaction")
                .put("url.scheme", "https")
                .put("url.query", "person=example-person-id&page=3&conversationId=example-conversation-id")
                .put("server.address", "localhost")
                .build(), filteredAttributes);
    }

    @Test
    public void testRETITAttributesAreAlwaysExcluded() {
        System.setProperty(Constants.RETIT_METRICS_EXCLUDED_ATTRIBUTES_CONFIGURATION_PROPERTY, "");

        Attributes filteredAttributes = MetricPublishingService.getAttributesWithoutExcludedAttributes(SERVER_SPAN_ATTRIBUTES);

        Assertions.assertNull(filteredAttributes.get(AttributeKey.longKey("io.retit.startcputime")));
        Assertions.assertNull(filteredAttributes.get(AttributeKey.longKey("io.retit.endcputime")));
        Assertions.assertEquals(SERVER_SPAN_ATTRIBUTES.size() - 2, filteredAttributes.size());
    }
}
