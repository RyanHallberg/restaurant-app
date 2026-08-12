package com.ryanhallberg.restaurant.orders.dto;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String status,
        int totalCents,
        String paymentReference,
        Instant createdAt,
        List<OrderLineResponse> items) {

    public record OrderLineResponse(String itemName, int priceCents, int quantity) {
    }
}
