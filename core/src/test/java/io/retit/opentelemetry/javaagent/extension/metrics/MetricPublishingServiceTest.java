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

public class MetricPublishingServiceTest {

    // attributes of a typical server span created by the tomcat instrumentation
    private static final Attributes SERVER_SPAN_ATTRIBUTES = Attributes.builder()
            .put("http.request.method", "GET")
            .put("http.route", "/app/**")
            .put("http.response.status_code", 200L)
            .put("url.path", "/app/LebenslaufeintraegeAuflisten")
            .put("url.scheme", "https")
            .put("url.query", "Person=1000000084116765718&ausHauptNav=3&conversationId=cce315bfa49da6b4-167")
            .put("server.address", "localhost")
            .put("client.address", "130.249.64.241")
            .put("network.peer.address", "127.0.0.6")
            .put("network.peer.port", 40853L)
            .put("network.protocol.version", "1.1")
            .put("user_agent.original", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) HeadlessChrome/151.0.0.0")
            .put("thread.id", 124L)
            .put("thread.name", "http-nio-8442-exec-5")
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
                .put("url.path", "/app/LebenslaufeintraegeAuflisten")
                .put("url.scheme", "https")
                .put("server.address", "localhost")
                .build(), filteredAttributes);
    }

    @Test
    public void testConfiguredAttributesReplaceDefaultList() {
        System.setProperty(Constants.RETIT_METRICS_EXCLUDED_ATTRIBUTES_CONFIGURATION_PROPERTY, " network, thread, user,client ,");

        Attributes filteredAttributes = MetricPublishingService.getAttributesWithoutExcludedAttributes(SERVER_SPAN_ATTRIBUTES);

        Assertions.assertEquals(Attributes.builder()
                .put("http.request.method", "GET")
                .put("http.route", "/app/**")
                .put("http.response.status_code", 200L)
                .put("url.path", "/app/LebenslaufeintraegeAuflisten")
                .put("url.scheme", "https")
                .put("url.query", "Person=1000000084116765718&ausHauptNav=3&conversationId=cce315bfa49da6b4-167")
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
