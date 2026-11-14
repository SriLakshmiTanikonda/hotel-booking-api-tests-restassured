package com.qa.api.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.api.data.TestDataDb;
import com.qa.api.models.Booking;
import com.qa.api.models.CreatedBooking;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test input comes from SQL queries against the test-data database.
 * Each guest row becomes a booking through the API, and the API's
 * response is compared field by field with the database row.
 */
public class DataDrivenBookingTests extends BaseTest {

    private String token;
    private final List<Integer> createdIds = new CopyOnWriteArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void logIn() {
        token = client.token();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        createdIds.forEach(id -> client.delete(id, token));
    }

    @DataProvider(parallel = true)
    public Object[][] activeGuests() {
        List<Booking> bookings = TestDataDb.activeBookings();
        return bookings.stream().map(b -> new Object[] {b}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "activeGuests", groups = {"regression", "data"},
          description = "API stores each database guest exactly")
    public void apiMatchesDatabase(Booking fromDb) {
        CreatedBooking created = client.create(fromDb)
                .then().statusCode(200)
                .extract().as(CreatedBooking.class);
        createdIds.add(created.bookingid());

        Booking fromApi = client.get(created.bookingid()).as(Booking.class);
        assertThat(fromApi).isEqualTo(fromDb);

        // Cross-check: nights calculated from the API's dates must equal the SQL calculation.
        long apiNights = ChronoUnit.DAYS.between(
                LocalDate.parse(fromApi.bookingdates().checkin()),
                LocalDate.parse(fromApi.bookingdates().checkout()));
        assertThat(apiNights).isEqualTo(TestDataDb.nightsFor(fromDb.firstname(), fromDb.lastname()));
    }

    @Test(groups = {"data"}, description = "Retired rows are excluded from test input")
    public void retiredRowsAreExcluded() {
        assertThat(TestDataDb.activeBookings())
                .extracting(Booking::lastname)
                .doesNotContain("Record")
                .hasSize(4);
    }

    @Test(groups = {"data"}, description = "Deposit query filters and sorts correctly")
    public void depositQueryFiltersAndSorts() {
        List<Booking> paid = TestDataDb.bookingsWithDepositPaid(true);

        assertThat(paid).allMatch(Booking::depositpaid);
        assertThat(paid).extracting(Booking::totalprice).isSortedAccordingTo((a, b) -> b - a);
    }
}
