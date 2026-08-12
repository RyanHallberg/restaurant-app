package com.ryanhallberg.restaurant.reservations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ryanhallberg.restaurant.common.error.ConflictException;
import com.ryanhallberg.restaurant.reservations.dto.CreateReservationRequest;

class ReservationServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Chicago");
    // Fixed "now": 2026-08-12 15:00 local.
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-08-12T20:00:00Z"), ZONE);
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 12);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    private ReservationRepository repository;
    private ReservationService service;

    @BeforeEach
    void setUp() {
        repository = mock(ReservationRepository.class);
        var properties = new ReservationProperties(10, 30, LocalTime.of(11, 0), LocalTime.of(22, 0));
        service = new ReservationService(repository, properties, FIXED_CLOCK);
    }

    @Test
    void slotsRunEveryThirtyMinutesFromOpeningWithLastSeatingBeforeClosing() {
        var slots = service.slotTimes();

        assertThat(slots).hasSize(22);
        assertThat(slots.getFirst()).isEqualTo(LocalTime.of(11, 0));
        assertThat(slots.getLast()).isEqualTo(LocalTime.of(21, 30));
    }

    @Test
    void availabilityMarksPastSlotsUnavailableToday() {
        when(repository.findByReservationDateAndStatus(TODAY, ReservationStatus.CONFIRMED))
                .thenReturn(List.of());

        var availability = service.availability(TODAY);

        var bySlot = availability.slots();
        // 15:00 fixed "now": morning slots are gone, evening slots remain.
        assertThat(bySlot.stream().filter(s -> s.time().equals("11:00")).findFirst().orElseThrow().available())
                .isFalse();
        assertThat(bySlot.stream().filter(s -> s.time().equals("15:00")).findFirst().orElseThrow().available())
                .isFalse();
        assertThat(bySlot.stream().filter(s -> s.time().equals("19:00")).findFirst().orElseThrow().available())
                .isTrue();
    }

    @Test
    void createRejectsTimesOffTheSlotGrid() {
        var request = request(TOMORROW, LocalTime.of(18, 15));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not a bookable slot");
    }

    @Test
    void createRejectsSlotsInThePast() {
        var request = request(TODAY, LocalTime.of(12, 0));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already passed");
    }

    @Test
    void createRejectsFullSlots() {
        when(repository.countByReservationDateAndReservationTimeAndStatus(
                TOMORROW, LocalTime.of(19, 0), ReservationStatus.CONFIRMED)).thenReturn(10L);

        assertThatThrownBy(() -> service.create(request(TOMORROW, LocalTime.of(19, 0))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("No tables left");
    }

    @Test
    void createSavesAndReturnsConfirmationCode() {
        when(repository.countByReservationDateAndReservationTimeAndStatus(
                TOMORROW, LocalTime.of(19, 0), ReservationStatus.CONFIRMED)).thenReturn(3L);
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request(TOMORROW, LocalTime.of(19, 0)));

        assertThat(response.confirmationCode()).hasSize(8).matches("[A-Z2-9]+");
        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(response.date()).isEqualTo(TOMORROW);
    }

    private static CreateReservationRequest request(LocalDate date, LocalTime time) {
        return new CreateReservationRequest(
                "Ada Lovelace", "ada@example.com", "555-0100", 4, date, time);
    }
}
