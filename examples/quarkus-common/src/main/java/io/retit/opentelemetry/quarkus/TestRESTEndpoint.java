package io.retit.opentelemetry.quarkus;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.io.IOException;

/**
 * This is an example REST service that provides three endpoints for HTTP GET / POST and DELETE.
 */
@Path("/test-rest-endpoint")
public class TestRESTEndpoint {

    @Inject
    private TestService testService;

    /**
     * Example GET endpoint.
     *
     * @return operation result including the computed value
     */
    @GET
    @Path("getData")
    public String getData() throws InterruptedException, IOException {
        return "GET " + testService.veryComplexBusinessFunction(3000, "GET");
    }

    /**
     * Example POST endpoint.
     *
     * @return operation result including the computed value
     */
    @POST
    @Path("postData")
    public String postData() throws InterruptedException, IOException {
        return "POST" + testService.veryComplexBusinessFunction(4000, "POST");
    }

    /**
     * Example DELETE endpoint.
     *
     * @return operation result including the computed value
     */
    @DELETE
    @Path("deleteData")
    public String deleteData() throws InterruptedException, IOException {
        return "DELETE" + testService.veryComplexBusinessFunction(6000, "DELETE");
    }

}
