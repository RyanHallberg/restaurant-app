package com.ryanhallberg.restaurant.common.web;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Project-owned page envelope. Controllers return this instead of Spring Data's
 * {@link Page}, whose JSON shape is not a stable public contract and generates
 * unwieldy OpenAPI types for frontend codegen.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
