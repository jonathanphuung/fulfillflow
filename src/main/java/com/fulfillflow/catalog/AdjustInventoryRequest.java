package com.fulfillflow.catalog;

import jakarta.validation.constraints.NotNull;

public record AdjustInventoryRequest(@NotNull Integer quantityChange) {
}
