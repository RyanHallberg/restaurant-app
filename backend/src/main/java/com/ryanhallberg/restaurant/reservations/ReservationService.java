package com.ryanhallberg.restaurant.reservations;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

import com.ryanhallberg.restaurant.common.error.ConflictException;
import com.ryanhallberg.restaurant.common.error.ForbiddenException;
import com.ryanhallberg.restaurant.common.error.NotFoundException;
import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.reservations.dto.AvailabilityResponse;
import com.ryanhallberg.restaurant.reservations.dto.CreateReservationRequest;
import com.ryanhallberg.restaurant.reservations.dto.ReservationResponse;
import com.ryanhallberg.restaurant.reservations.dto.UpdateReservationStatusRequest;

@Service
public class ReservationService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final ReservationRepository repository;
    private final ReservationProperties properties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    ReservationService(ReservationRepository repository, ReservationProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse availability(LocalDate date) {
        var bookedCounts = repository.findByReservationDateAndStatus(date, ReservationStatus.CONFIRMED);
        var now = LocalDateTime.now(clock);

        List<AvailabilityResponse.SlotResponse> slots = new ArrayList<>();
        for (LocalTime slot : slotTimes()) {
            boolean inPast = !date.atTime(slot).isAfter(now);
            long booked = bookedCounts.stream()
                    .filter(reservation -> reservation.getReservationTime().equals(slot))
                    .count();
            boolean available = !inPast && booked < properties.tablesPerSlot();
            slots.add(new AvailabilityResponse.SlotResponse(slot.toString(), available));
        }
        return new AvailabilityResponse(date, slots);
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest request, @Nullable Long userId) {
        var slot = request.time();
        if (!slotTimes().contains(slot)) {
            throw new ConflictException(
                    "Time %s is not a bookable slot".formatted(slot));
        }
        if (!request.date().atTime(slot).isAfter(LocalDateTime.now(clock))) {
            throw new ConflictException("That time has already passed");
        }

        repository.lockSlot(request.date() + "T" + slot);
        long booked = repository.countByReservationDateAndReservationTimeAndStatus(
                request.date(), slot, ReservationStatus.CONFIRMED);
        if (booked >= properties.tablesPerSlot()) {
            throw new ConflictException(
                    "No tables left at %s on %s".formatted(slot, request.date()));
        }

        var reservation = repository.save(new Reservation(
                userId,
                request.customerName(),
                request.customerEmail(),
                request.customerPhone(),
                request.partySize(),
                request.date(),
                slot,
                generateCode()));
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> myReservations(long userId, Pageable pageable) {
        return PageResponse.from(repository.findByUserId(userId, pageable).map(ReservationService::toResponse));
    }

    @Transactional
    public ReservationResponse cancel(long id, long userId, boolean admin) {
        var reservation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation %d not found".formatted(id)));
        if (!admin && !Long.valueOf(userId).equals(reservation.getUserId())) {
            throw new ForbiddenException("This reservation belongs to someone else");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed reservations can be cancelled");
        }
        reservation.updateStatus(ReservationStatus.CANCELLED);
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> adminList(
            @Nullable LocalDate date, @Nullable ReservationStatus status, Pageable pageable) {
        var page = date != null && status != null ? repository.findByReservationDateAndStatus(date, status, pageable)
                : date != null ? repository.findByReservationDate(date, pageable)
                : status != null ? repository.findByStatus(status, pageable)
                : repository.findAll(pageable);
        return PageResponse.from(page.map(ReservationService::toResponse));
    }

    @Transactional
    public ReservationResponse updateStatus(long id, UpdateReservationStatusRequest request) {
        var reservation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation %d not found".formatted(id)));
        ReservationStatus target;
        try {
            target = ReservationStatus.valueOf(request.status());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Unknown status: " + request.status());
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED || target == ReservationStatus.CONFIRMED) {
            throw new ConflictException("Cannot move a %s reservation to %s"
                    .formatted(reservation.getStatus(), target));
        }
        reservation.updateStatus(target);
        return toResponse(reservation);
    }

    List<LocalTime> slotTimes() {
        List<LocalTime> slots = new ArrayList<>();
        var time = properties.openingTime();
        // Last seating is one slot before closing.
        while (time.isBefore(properties.closingTime())) {
            slots.add(time);
            time = time.plusMinutes(properties.slotMinutes());
        }
        return slots;
    }

    private String generateCode() {
        var code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }

    private static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getPartySize(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getStatus().name(),
                reservation.getConfirmationCode());
    }
}
