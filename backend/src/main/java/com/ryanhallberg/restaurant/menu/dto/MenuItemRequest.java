package com.ryanhallberg.restaurant.menu.dto;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record MenuItemRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 100) String name,
        @Nullable @Size(max = 1000) String description,
        // $1000 ceiling per dish: keeps order-total math trivially overflow-safe.
        @NotNull @PositiveOrZero @Max(100_000) Integer priceCents,
        @Nullable @Size(max = 500) String imageUrl,
        @NotNull Boolean available) {
}
