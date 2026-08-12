package com.ryanhallberg.restaurant.menu.dto;

import org.jspecify.annotations.Nullable;

public record MenuItemResponse(
        Long id,
        Long categoryId,
        String name,
        @Nullable String description,
        int priceCents,
        @Nullable String imageUrl,
        boolean available) {
}
