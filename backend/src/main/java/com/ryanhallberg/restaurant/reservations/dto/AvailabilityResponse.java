package com.ryanhallberg.restaurant.reservations.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(LocalDate date, List<SlotResponse> slots) {

    public record SlotResponse(String time, boolean available) {
    }
}
