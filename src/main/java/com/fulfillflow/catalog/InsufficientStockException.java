package com.fulfillflow.catalog;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, int requestedChange, int availableQuantity) {
        super("Cannot adjust product %s by %d; only %d units are available"
                .formatted(productId, requestedChange, availableQuantity));
    }
}
