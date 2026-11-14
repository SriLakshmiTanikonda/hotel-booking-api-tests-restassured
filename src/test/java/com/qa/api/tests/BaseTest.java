package com.qa.api.tests;

import com.qa.api.clients.BookingClient;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;

public abstract class BaseTest {

    protected final BookingClient client = new BookingClient();

    /** Prints the full request and response only for failing checks, keeping CI logs readable. */
    @BeforeSuite(alwaysRun = true)
    public void configureRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
