package com.qa.api.tests;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import com.qa.api.data.BookingFactory;
import com.qa.api.models.CreatedBooking;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Security and error handling: what the API must refuse. */
public class BookingNegativeTests extends BaseTest {

    private String token;
    private int bookingId;

    @BeforeClass(alwaysRun = true)
    public void createTarget() {
        token = client.token();
        bookingId = client.create(BookingFactory.valid())
                .then().statusCode(200)
                .extract().as(CreatedBooking.class)
                .bookingid();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        client.delete(bookingId, token);
    }

    @Test(groups = {"smoke", "security"}, description = "Update without a token is forbidden")
    public void updateWithoutTokenIsForbidden() {
        client.updateWithoutAuth(bookingId, BookingFactory.valid().withFirstname("Hacker"))
                .then().statusCode(403);
    }

    @Test(groups = {"security"}, description = "Delete without a token is forbidden")
    public void deleteWithoutTokenIsForbidden() {
        client.deleteWithoutAuth(bookingId).then().statusCode(403);
        // The booking must still exist after the refused delete.
        client.get(bookingId).then().statusCode(200);
    }

    @Test(groups = {"security"}, description = "A made-up token is forbidden")
    public void fakeTokenIsForbidden() {
        client.update(bookingId, BookingFactory.valid(), "not-a-real-token").then().statusCode(403);
    }

    @Test(groups = {"regression"}, description = "Unknown booking ID returns 404")
    public void unknownIdReturns404() {
        client.get(999_999_999).then().statusCode(404);
    }

    @DataProvider
    public Object[][] invalidBodies() {
        return new Object[][] {
                {"missing required fields", "{\"firstname\": \"OnlyName\"}"},
                {"malformed JSON", "{\"firstname\": \"A\","},
                {"empty body", "{}"},
        };
    }

    /**
     * A well-behaved API returns 400 for bad input. Restful Booker returns 500
     * for some of these, which the README lists as a defect. The test accepts
     * either error code but fails if a bad request is ever accepted with 200.
     */
    @Test(dataProvider = "invalidBodies", groups = {"regression"}, description = "Invalid bodies are refused")
    public void invalidBodyIsRefused(String caseName, String body) {
        client.createRaw(body).then().statusCode(anyOf(is(400), is(500)));
    }
}
