package io.retit.spring.carbon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * This is an example REST service that provides three endpoints for HTTP GET / POST and DELETE.
 */
@RestController
@RequestMapping("/test-rest-endpoint")
public class TestRESTEndpoint {

    @Autowired
    private TestService testService;

    @GetMapping
    @RequestMapping("getData")
    public String getData() throws InterruptedException, IOException {
        return "GET " + testService.veryComplexBusinessFunction(3000);
    }

    @PostMapping
    @RequestMapping("postData")
    public String postData() throws InterruptedException, IOException {
        return "POST" + testService.veryComplexBusinessFunction(4000);
    }

    @DeleteMapping
    @RequestMapping("deleteData")
    public String deleteData() throws InterruptedException, IOException {
        return "DELETE" + testService.veryComplexBusinessFunction(6000);
    }
}
