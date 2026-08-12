package com.ryanhallberg.restaurant.reservations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ReservationRepository extends JpaRepository<Reservation, Long> {

    long countByReservationDateAndReservationTimeAndStatus(
            LocalDate date, LocalTime time, ReservationStatus status);

    List<Reservation> findByReservationDateAndStatus(LocalDate date, ReservationStatus status);

    /**
     * Serializes concurrent bookings for the same slot within their
     * transactions, closing the count-then-insert race without a lock table.
     */
    @Query(value = "SELECT pg_advisory_xact_lock(hashtext(:slotKey))", nativeQuery = true)
    void lockSlot(@Param("slotKey") String slotKey);
}
