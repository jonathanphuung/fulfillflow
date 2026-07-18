package com.fulfillflow.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank @Size(max = 160) String customerName,
        @NotEmpty List<@Valid Item> items) {

    public record Item(@NotNull UUID productId, @Min(1) int quantity) {
    }
}
