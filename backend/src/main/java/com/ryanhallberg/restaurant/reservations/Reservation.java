package com.ryanhallberg.restaurant.reservations;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "confirmation_code", nullable = false, unique = true, length = 12)
    private String confirmationCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Reservation() {
    }

    Reservation(Long userId, String customerName, String customerEmail, String customerPhone,
            int partySize, LocalDate reservationDate, LocalTime reservationTime, String confirmationCode) {
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.partySize = partySize;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.confirmationCode = confirmationCode;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    Long getId() {
        return id;
    }

    Long getUserId() {
        return userId;
    }

    void updateStatus(ReservationStatus newStatus) {
        this.status = newStatus;
    }

    String getCustomerName() {
        return customerName;
    }

    String getCustomerEmail() {
        return customerEmail;
    }

    int getPartySize() {
        return partySize;
    }

    LocalDate getReservationDate() {
        return reservationDate;
    }

    LocalTime getReservationTime() {
        return reservationTime;
    }

    ReservationStatus getStatus() {
        return status;
    }

    String getConfirmationCode() {
        return confirmationCode;
    }
}
