package com.qa.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Request and response body for a booking. Field names match the API's
 * JSON exactly, so Jackson maps them without annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Booking(
        String firstname,
        String lastname,
        int totalprice,
        boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds) {

    public Booking withFirstname(String newFirstname) {
        return new Booking(newFirstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }

    public Booking withTotalprice(int newTotalprice) {
        return new Booking(firstname, lastname, newTotalprice, depositpaid, bookingdates, additionalneeds);
    }
}
