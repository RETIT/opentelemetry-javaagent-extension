package io.retit.opentelemetry.javaagent.extension.processor;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Class responsible to retrieve Data from given Platform, to provide it to theRETITSpanProcessor
 */
public class DataProvider {

    double returnValue;

    /**
     * Method to retrieve data from the given platform
     *
     * @return double
     */

    static double dataRetrieval() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://www.randomnumberapi.com/api/v1.0/random?min=1&;max=10&count=1"))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            System.out.println("Random Number: " + responseBody);
            responseBody = responseBody.replace("[", "").replace("]", "");
            return Double.parseDouble(responseBody);
        } catch (IOException | InterruptedException e) {
            System.out.println("error happened");
            return 0;
        }
    }
}
