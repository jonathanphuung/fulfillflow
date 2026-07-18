package com.fulfillflow.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory")
class Inventory {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    @Version
    private long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Inventory() {
    }

    Inventory(Product product, int quantityOnHand) {
        this.product = product;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = 0;
        this.updatedAt = Instant.now();
    }

    int getQuantityOnHand() {
        return quantityOnHand;
    }

    int getQuantityReserved() {
        return quantityReserved;
    }

    UUID getProductId() {
        return productId;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void adjustOnHand(int quantityChange) {
        var updatedQuantity = Math.addExact(quantityOnHand, quantityChange);
        if (updatedQuantity < quantityReserved) {
            throw new InsufficientStockException(productId, quantityChange, availableQuantity());
        }
        quantityOnHand = updatedQuantity;
        updatedAt = Instant.now();
    }

    private int availableQuantity() {
        return quantityOnHand - quantityReserved;
    }
}
