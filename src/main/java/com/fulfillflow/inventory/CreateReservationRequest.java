package com.fulfillflow.inventory;

import jakarta.validation.constraints.Min;

public record CreateReservationRequest(@Min(1) int quantity) {
}
