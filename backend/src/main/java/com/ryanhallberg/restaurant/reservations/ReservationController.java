package com.ryanhallberg.restaurant.reservations;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.reservations.dto.AvailabilityResponse;
import com.ryanhallberg.restaurant.reservations.dto.CreateReservationRequest;
import com.ryanhallberg.restaurant.reservations.dto.ReservationResponse;
import com.ryanhallberg.restaurant.reservations.dto.UpdateReservationStatusRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Table availability, booking, and admin management")
public class ReservationController {

    private static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

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
    @Operation(summary = "Book a table (guests welcome); returns a confirmation code. 409 when the slot is full")
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal @Nullable Jwt jwt) {
        Long userId = jwt == null ? null : Long.parseLong(jwt.getSubject());
        return reservationService.create(request, userId);
    }

    @GetMapping("/my")
    @Operation(summary = "The caller's reservations", security = @SecurityRequirement(name = "bearerAuth"))
    public PageResponse<ReservationResponse> myReservations(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable) {
        return reservationService.myReservations(Long.parseLong(jwt.getSubject()), pageable);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation (owner or admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ReservationResponse cancel(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        boolean admin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(ROLE_ADMIN);
        return reservationService.cancel(id, Long.parseLong(jwt.getSubject()), admin);
    }

    @GetMapping
    @Operation(summary = "List reservations, filterable by date and status (admin)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public PageResponse<ReservationResponse> adminList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable) {
        return reservationService.adminList(date, status, pageable);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Move a confirmed reservation to CANCELLED or COMPLETED (admin)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ReservationResponse updateStatus(
            @PathVariable long id, @Valid @RequestBody UpdateReservationStatusRequest request) {
        return reservationService.updateStatus(id, request);
    }
}
