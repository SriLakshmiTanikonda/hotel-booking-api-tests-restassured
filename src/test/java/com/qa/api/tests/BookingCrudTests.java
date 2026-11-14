package com.qa.api.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import com.qa.api.data.BookingFactory;
import com.qa.api.models.Booking;
import com.qa.api.models.CreatedBooking;
import com.qa.api.specs.Specs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Create, read, update and delete. Each test makes its own booking and the
 * cleanup step deletes it, so tests are independent and can run in any order.
 */
public class BookingCrudTests extends BaseTest {

    private String token;
    private final List<Integer> createdIds = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void logIn() {
        token = client.token();
    }

    @AfterMethod(alwaysRun = true)
    public void deleteCreatedBookings() {
        for (int id : createdIds) {
            client.delete(id, token);
        }
        createdIds.clear();
    }

    private CreatedBooking createBooking(Booking booking) {
        CreatedBooking created = client.create(booking)
                .then().statusCode(200)
                .extract().as(CreatedBooking.class);
        createdIds.add(created.bookingid());
        return created;
    }

    @Test(groups = {"smoke"}, description = "New booking is stored exactly as sent")
    public void createBooking() {
        Booking booking = BookingFactory.valid();

        CreatedBooking created = client.create(booking)
                .then()
                .spec(Specs.withinTimeBudget())
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/created-booking-schema.json"))
                .extract().as(CreatedBooking.class);
        createdIds.add(created.bookingid());

        assertThat(created.booking()).isEqualTo(booking);
    }

    @Test(groups = {"smoke"}, description = "Created booking can be read back and matches the contract")
    public void readBooking() {
        Booking booking = BookingFactory.valid();
        int id = createBooking(booking).bookingid();

        Booking fetched = client.get(id)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
                .extract().as(Booking.class);

        assertThat(fetched).isEqualTo(booking);
    }

    @Test(groups = {"regression"}, description = "Search by name finds the new booking")
    public void searchFindsBooking() {
        Booking booking = BookingFactory.valid();
        int id = createBooking(booking).bookingid();

        client.findByName(booking.firstname(), booking.lastname())
                .then()
                .statusCode(200)
                .body("bookingid", hasItem(id));
    }

    @Test(groups = {"regression"}, description = "Full update replaces every field")
    public void updateBooking() {
        int id = createBooking(BookingFactory.valid()).bookingid();
        Booking changed = BookingFactory.valid().withFirstname("Updated").withTotalprice(999);

        Booking updated = client.update(id, changed, token)
                .then().statusCode(200)
                .extract().as(Booking.class);

        assertThat(updated).isEqualTo(changed);
        assertThat(client.get(id).as(Booking.class)).isEqualTo(changed);
    }

    @Test(groups = {"regression"}, description = "Partial update changes only the fields sent")
    public void partialUpdateBooking() {
        Booking original = BookingFactory.valid();
        int id = createBooking(original).bookingid();

        Map<String, Object> changes = Map.of("additionalneeds", "Late checkout", "totalprice", 500);
        client.partialUpdate(id, changes, token)
                .then()
                .statusCode(200)
                .body("additionalneeds", equalTo("Late checkout"))
                .body("totalprice", equalTo(500))
                // Untouched fields must keep their original values.
                .body("firstname", equalTo(original.firstname()))
                .body("lastname", equalTo(original.lastname()))
                .body("bookingdates.checkin", equalTo(original.bookingdates().checkin()));
    }

    @Test(groups = {"regression"}, description = "Deleted booking can no longer be read")
    public void deleteBooking() {
        int id = createBooking(BookingFactory.valid()).bookingid();

        // Restful Booker answers a successful delete with 201 Created (a known quirk).
        client.delete(id, token).then().statusCode(201);
        createdIds.remove(Integer.valueOf(id));

        client.get(id).then().statusCode(404);
    }
}
