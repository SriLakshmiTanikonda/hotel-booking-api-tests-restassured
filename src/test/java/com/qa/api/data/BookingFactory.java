package com.qa.api.data;

import com.qa.api.models.Booking;
import com.qa.api.models.BookingDates;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Builds unique bookings for tests that create their own data. A random
 * suffix on the last name means parallel runs on the shared demo server
 * never pick up each other's records.
 */
public final class BookingFactory {

    private BookingFactory() {
    }

    public static Booking valid() {
        LocalDate checkin = LocalDate.now().plusDays(30);
        return new Booking(
                "Test",
                "Guest-" + UUID.randomUUID().toString().substring(0, 8),
                350,
                true,
                new BookingDates(checkin.toString(), checkin.plusDays(3).toString()),
                "Breakfast");
    }
}
