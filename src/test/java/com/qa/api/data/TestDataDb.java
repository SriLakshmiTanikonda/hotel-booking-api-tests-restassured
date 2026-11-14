package com.qa.api.data;

import com.qa.api.models.Booking;
import com.qa.api.models.BookingDates;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data source backed by SQL. Tests pull their input from the database
 * with queries, then check that the API returns exactly what the database says.
 */
public final class TestDataDb {

    private static final String URL = "jdbc:h2:mem:testdata;DB_CLOSE_DELAY=-1";
    private static boolean initialized;

    private TestDataDb() {
    }

    private static synchronized Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, "sa", "");
        if (!initialized) {
            runScript(connection, "sql/schema.sql");
            runScript(connection, "sql/data.sql");
            initialized = true;
        }
        return connection;
    }

    private static void runScript(Connection connection, String resource) throws SQLException {
        String sql;
        try (InputStream in = TestDataDb.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing SQL resource: " + resource);
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resource, e);
        }
        try (Statement statement = connection.createStatement()) {
            for (String part : sql.split(";")) {
                String trimmed = part.replaceAll("(?m)^\\s*--.*$", "").trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        }
    }

    /** Active guests only: retired rows must never be used as test input. */
    public static List<Booking> activeBookings() {
        String query = """
                SELECT first_name, last_name, total_price, deposit_paid,
                       check_in, check_out,
                       COALESCE(additional_needs, 'None') AS additional_needs
                FROM guest_bookings
                WHERE active = TRUE
                ORDER BY id
                """;
        return queryBookings(query);
    }

    public static List<Booking> bookingsWithDepositPaid(boolean paid) {
        String query = """
                SELECT first_name, last_name, total_price, deposit_paid,
                       check_in, check_out,
                       COALESCE(additional_needs, 'None') AS additional_needs
                FROM guest_bookings
                WHERE active = TRUE AND deposit_paid = ?
                ORDER BY total_price DESC
                """;
        return queryBookings(query, paid);
    }

    /** Number of nights for a guest, computed in SQL, used to cross-check the API's dates. */
    public static int nightsFor(String firstName, String lastName) {
        String query = """
                SELECT DATEDIFF(DAY, check_in, check_out) AS nights
                FROM guest_bookings
                WHERE first_name = ? AND last_name = ?
                """;
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("No guest named " + firstName + " " + lastName);
                }
                return rs.getInt("nights");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Query failed", e);
        }
    }

    private static List<Booking> queryBookings(String query, Object... params) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            List<Booking> bookings = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    bookings.add(new Booking(
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getInt("total_price"),
                            rs.getBoolean("deposit_paid"),
                            new BookingDates(
                                    rs.getDate("check_in").toLocalDate().toString(),
                                    rs.getDate("check_out").toLocalDate().toString()),
                            rs.getString("additional_needs")));
                }
            }
            return bookings;
        } catch (SQLException e) {
            throw new IllegalStateException("Query failed", e);
        }
    }
}
