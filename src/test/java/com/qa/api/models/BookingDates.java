package com.qa.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Dates use the API's yyyy-MM-dd text format. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingDates(String checkin, String checkout) {
}
