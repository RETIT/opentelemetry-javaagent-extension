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

package io.retit.opentelemetry.quarkus.library;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.io.IOException;

/**
 * Example REST service providing GET, POST and DELETE endpoints.
 *
 * <p>Resource-demand measurement is performed automatically by the {@code RETITSpanProcessor}
 * that the {@code opentelemetry-java-agent-extension-library} registers as a CDI bean.
 * No Java-agent is required – adding the library as a Maven dependency is sufficient.</p>
 */
@Path("/test-rest-endpoint")
public class TestRESTEndpoint {
    @Inject
    private TestService testService;

    /**
     * GET endpoint.
     *
     * @return response string.
     * @throws InterruptedException if interrupted.
     * @throws IOException          if an I/O error occurs.
     */
    @GET
    @Path("getData")
    public String getData() throws InterruptedException, IOException {
        return "GET " + testService.veryComplexBusinessFunction(3000, "GET");
    }

    /**
     * POST endpoint.
     *
     * @return response string.
     * @throws InterruptedException if interrupted.
     * @throws IOException          if an I/O error occurs.
     */
    @POST
    @Path("postData")
    public String postData() throws InterruptedException, IOException {
        return "POST" + testService.veryComplexBusinessFunction(4000, "POST");
    }

    /**
     * DELETE endpoint.
     *
     * @return response string.
     * @throws InterruptedException if interrupted.
     * @throws IOException          if an I/O error occurs.
     */
    @DELETE
    @Path("deleteData")
    public String deleteData() throws InterruptedException, IOException {
        return "DELETE" + testService.veryComplexBusinessFunction(6000, "DELETE");
    }
}