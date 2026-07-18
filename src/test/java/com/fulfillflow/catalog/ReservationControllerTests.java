package com.fulfillflow.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository products;

    @Test
    void reservesAndReleasesStockIdempotently() throws Exception {
        var product = products.saveAndFlush(new Product("CART-1", "Picking Cart", null, 5));

        var firstResponse = mockMvc.perform(post("/api/inventory/{productId}/reservations", product.getId())
                        .header("Idempotency-Key", "order-100-item-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String reservationId = JsonPath.read(firstResponse, "$.id");

        mockMvc.perform(post("/api/inventory/{productId}/reservations", product.getId())
                        .header("Idempotency-Key", "order-100-item-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reservationId));

        mockMvc.perform(get("/api/inventory/{productId}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityReserved").value(3))
                .andExpect(jsonPath("$.quantityAvailable").value(2));

        mockMvc.perform(delete("/api/inventory/reservations/{reservationId}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));

        mockMvc.perform(delete("/api/inventory/reservations/{reservationId}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));

        mockMvc.perform(get("/api/inventory/{productId}", product.getId()))
                .andExpect(jsonPath("$.quantityReserved").value(0));
    }

    @Test
    void rejectsReservationBeyondAvailableStock() throws Exception {
        var product = products.saveAndFlush(new Product("CART-2", "Utility Cart", null, 2));

        mockMvc.perform(post("/api/inventory/{productId}/reservations", product.getId())
                        .header("Idempotency-Key", "order-101-item-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":3}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }
}
