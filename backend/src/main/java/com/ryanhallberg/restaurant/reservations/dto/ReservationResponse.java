package com.ryanhallberg.restaurant.reservations.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        Long id,
        String customerName,
        int partySize,
        LocalDate date,
        LocalTime time,
        String status,
        String confirmationCode) {
}
