package com.ryanhallberg.restaurant.reservations.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReservationRequest(
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank @Email @Size(max = 255) String customerEmail,
        @NotBlank @Size(max = 30) String customerPhone,
        @Min(1) @Max(12) int partySize,
        // Past-date rejection lives in the service, which runs on the injected
        // restaurant-zone Clock; @FutureOrPresent would re-check it on the
        // JVM-default zone and disagree near midnight.
        @NotNull LocalDate date,
        @NotNull LocalTime time) {
}
