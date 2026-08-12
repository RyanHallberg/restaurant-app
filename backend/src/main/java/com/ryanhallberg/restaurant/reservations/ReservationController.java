package com.ryanhallberg.restaurant.reservations;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ryanhallberg.restaurant.reservations.dto.AvailabilityResponse;
import com.ryanhallberg.restaurant.reservations.dto.CreateReservationRequest;
import com.ryanhallberg.restaurant.reservations.dto.ReservationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Table availability and booking")
public class ReservationController {

    private final ReservationService reservationService;

    ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/availability")
    @Operation(summary = "List 30-minute seating slots for a date with availability")
    public AvailabilityResponse availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reservationService.availability(date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book a table; returns a confirmation code. 409 when the slot is full")
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
        return reservationService.create(request);
    }
}
