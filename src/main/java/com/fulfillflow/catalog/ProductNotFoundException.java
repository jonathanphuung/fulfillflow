package com.fulfillflow.catalog;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID id) {
        super("Product " + id + " was not found");
    }
}
