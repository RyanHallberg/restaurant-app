package com.ryanhallberg.restaurant.orders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "total_cents", nullable = false)
    private int totalCents;

    @Column(name = "payment_reference", nullable = false, length = 50)
    private String paymentReference;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    Order(Long userId, int totalCents, String paymentReference) {
        this.userId = userId;
        this.totalCents = totalCents;
        this.paymentReference = paymentReference;
    }

    void addItem(OrderItem item) {
        items.add(item);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    Long getId() {
        return id;
    }

    Long getUserId() {
        return userId;
    }

    OrderStatus getStatus() {
        return status;
    }

    void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    int getTotalCents() {
        return totalCents;
    }

    String getPaymentReference() {
        return paymentReference;
    }

    List<OrderItem> getItems() {
        return items;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
