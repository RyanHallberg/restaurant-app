package com.ryanhallberg.restaurant.reservations.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateReservationStatusRequest(@NotBlank String status) {
}
