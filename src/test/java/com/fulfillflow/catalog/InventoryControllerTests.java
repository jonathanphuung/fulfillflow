package com.fulfillflow.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class InventoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository products;

    @Test
    void adjustsAvailableStock() throws Exception {
        var product = products.saveAndFlush(new Product("TOTE-1", "Picking Tote", null, 10));

        mockMvc.perform(patch("/api/inventory/{productId}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantityChange":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityOnHand").value(15))
                .andExpect(jsonPath("$.quantityAvailable").value(15));

        mockMvc.perform(get("/api/inventory/{productId}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityOnHand").value(15));
    }

    @Test
    void rejectsAdjustmentBelowZero() throws Exception {
        var product = products.saveAndFlush(new Product("TOTE-2", "Small Tote", null, 3));

        mockMvc.perform(patch("/api/inventory/{productId}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantityChange":-4}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(get("/api/inventory/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }
}
