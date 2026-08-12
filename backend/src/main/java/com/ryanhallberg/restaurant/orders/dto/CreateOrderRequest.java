package com.ryanhallberg.restaurant.orders.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotEmpty @Size(max = 50) @Valid List<OrderLineRequest> items,
        @NotNull @Valid PaymentRequest payment) {

    public record OrderLineRequest(
            @NotNull Long menuItemId,
            @Min(1) @Max(20) int quantity) {
    }

    /** Mock payment: shape-validated, never persisted, never logged. */
    public record PaymentRequest(
            @NotNull @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits") String cardNumber,
            @NotNull @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Expiry must be MM/YY") String expiry,
            @NotNull @Pattern(regexp = "\\d{3,4}", message = "CVC must be 3 or 4 digits") String cvc) {
    }
}
