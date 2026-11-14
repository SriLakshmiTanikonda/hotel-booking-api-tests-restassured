package com.qa.api.specs;

import static org.hamcrest.Matchers.lessThan;

import com.qa.api.config.ApiConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.concurrent.TimeUnit;

/**
 * Shared request and response specifications, so every call gets the same
 * base URI, JSON headers and response-time check.
 */
public final class Specs {

    private Specs() {
    }

    public static RequestSpecification request() {
        return new RequestSpecBuilder()
                .setBaseUri(ApiConfig.baseUri())
                .setContentType(ContentType.JSON)
                .setAccept("application/json")
                .build();
    }

    /** Adds the session token as a cookie, which Restful Booker needs for update and delete. */
    public static RequestSpecification authorized(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(request())
                .addCookie("token", token)
                .build();
    }

    public static ResponseSpecification withinTimeBudget() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(ApiConfig.maxResponseTimeMs()), TimeUnit.MILLISECONDS)
                .build();
    }
}
