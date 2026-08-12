package com.ryanhallberg.restaurant.menu;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_items")
class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MenuItem() {
    }

    MenuItem(MenuCategory category, String name, String description, int priceCents,
            String imageUrl, boolean available) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    void update(MenuCategory category, String name, String description, int priceCents,
            String imageUrl, boolean available) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
        this.available = available;
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

    MenuCategory getCategory() {
        return category;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    int getPriceCents() {
        return priceCents;
    }

    String getImageUrl() {
        return imageUrl;
    }

    boolean isAvailable() {
        return available;
    }
}
