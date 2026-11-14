package com.qa.api.tests;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.qa.api.config.ApiConfig;
import com.qa.api.specs.Specs;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AuthTests extends BaseTest {

    @Test(groups = {"smoke"}, description = "Health check endpoint is up")
    public void pingReturns201() {
        // Restful Booker returns 201 Created for a health check (a known quirk of this API).
        client.ping().then().statusCode(201);
    }

    @Test(groups = {"smoke"}, description = "Valid credentials return a token")
    public void validCredentialsReturnToken() {
        client.createToken(ApiConfig.username(), ApiConfig.password())
                .then()
                .spec(Specs.withinTimeBudget())
                .statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    @DataProvider
    public Object[][] badCredentials() {
        return new Object[][] {
                {"admin", "wrong-password"},
                {"not-a-user", "password123"},
                {"", ""},
        };
    }

    /**
     * This API answers bad credentials with HTTP 200 and a "reason" field
     * instead of 401. The test pins that behavior so a change is noticed,
     * and it is written up in the README as a defect worth raising.
     */
    @Test(dataProvider = "badCredentials", groups = {"regression"}, description = "Bad credentials never return a token")
    public void badCredentialsAreRejected(String username, String password) {
        client.createToken(username, password)
                .then()
                .statusCode(200)
                .body("token", nullValue())
                .body("reason", equalTo("Bad credentials"));
    }
}
