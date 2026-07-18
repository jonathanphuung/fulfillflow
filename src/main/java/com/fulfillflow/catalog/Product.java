package com.fulfillflow.catalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
class Product {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private Inventory inventory;

    protected Product() {
    }

    Product(String sku, String name, String description, int initialQuantity) {
        var now = Instant.now();
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
        this.inventory = new Inventory(this, initialQuantity);
    }

    UUID getId() {
        return id;
    }

    String getSku() {
        return sku;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    boolean isActive() {
        return active;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Inventory getInventory() {
        return inventory;
    }
}
