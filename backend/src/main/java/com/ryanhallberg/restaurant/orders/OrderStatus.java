package com.ryanhallberg.restaurant.orders;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> LEGAL_TRANSITIONS = Map.of(
            PLACED, Set.of(PREPARING, CANCELLED),
            PREPARING, Set.of(READY, CANCELLED),
            READY, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of());

    boolean canTransitionTo(OrderStatus target) {
        return LEGAL_TRANSITIONS.get(this).contains(target);
    }
}
