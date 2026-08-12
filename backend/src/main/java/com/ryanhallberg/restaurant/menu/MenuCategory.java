package com.ryanhallberg.restaurant.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_categories")
class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected MenuCategory() {
    }

    MenuCategory(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }

    void update(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }

    Long getId() {
        return id;
    }

    String getName() {
        return name;
    }

    int getDisplayOrder() {
        return displayOrder;
    }
}
