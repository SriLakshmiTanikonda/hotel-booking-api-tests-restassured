package com.qa.api.clients;

import static io.restassured.RestAssured.given;

import com.qa.api.config.ApiConfig;
import com.qa.api.models.Booking;
import com.qa.api.specs.Specs;
import io.restassured.response.Response;
import java.util.Map;

/**
 * One method per endpoint. Tests describe behavior ("create a booking")
 * and never build URLs, headers or cookies themselves.
 */
public class BookingClient {

    public Response ping() {
        return given(Specs.request()).when().get("/ping");
    }

    public Response createToken(String username, String password) {
        return given(Specs.request())
                .body(Map.of("username", username, "password", password))
                .when().post("/auth");
    }

    /** Logs in with the configured account and returns the token, failing fast if login breaks. */
    public String token() {
        String token = createToken(ApiConfig.username(), ApiConfig.password())
                .then().statusCode(200)
                .extract().path("token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Login did not return a token. Check the API credentials.");
        }
        return token;
    }

    public Response create(Booking booking) {
        return given(Specs.request()).body(booking).when().post("/booking");
    }

    public Response get(int id) {
        return given(Specs.request()).when().get("/booking/{id}", id);
    }

    public Response listIds() {
        return given(Specs.request()).when().get("/booking");
    }

    public Response findByName(String firstname, String lastname) {
        return given(Specs.request())
                .queryParam("firstname", firstname)
                .queryParam("lastname", lastname)
                .when().get("/booking");
    }

    public Response update(int id, Booking booking, String token) {
        return given(Specs.authorized(token)).body(booking).when().put("/booking/{id}", id);
    }

    public Response updateWithoutAuth(int id, Booking booking) {
        return given(Specs.request()).body(booking).when().put("/booking/{id}", id);
    }

    public Response partialUpdate(int id, Map<String, Object> changes, String token) {
        return given(Specs.authorized(token)).body(changes).when().patch("/booking/{id}", id);
    }

    public Response delete(int id, String token) {
        return given(Specs.authorized(token)).when().delete("/booking/{id}", id);
    }

    public Response deleteWithoutAuth(int id) {
        return given(Specs.request()).when().delete("/booking/{id}", id);
    }

    /** Sends a raw JSON string, used for malformed or incomplete bodies. */
    public Response createRaw(String json) {
        return given(Specs.request()).body(json).when().post("/booking");
    }
}
