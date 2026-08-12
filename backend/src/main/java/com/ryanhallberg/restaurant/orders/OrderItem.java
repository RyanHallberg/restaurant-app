package com.ryanhallberg.restaurant.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "menu_item_id")
    private Long menuItemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {
    }

    OrderItem(Order order, Long menuItemId, String itemName, int priceCents, int quantity) {
        this.order = order;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.priceCents = priceCents;
        this.quantity = quantity;
    }

    String getItemName() {
        return itemName;
    }

    int getPriceCents() {
        return priceCents;
    }

    int getQuantity() {
        return quantity;
    }
}
