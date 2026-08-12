package com.ryanhallberg.restaurant.reservations;

import java.time.LocalTime;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reservations")
record ReservationProperties(
        int tablesPerSlot,
        int slotMinutes,
        LocalTime openingTime,
        LocalTime closingTime) {
}
